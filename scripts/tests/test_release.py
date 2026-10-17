"""Exercise release promotion, rollback, and archive rejection without a cloud account."""
import io,json,os,subprocess,tarfile,tempfile,unittest
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]

class ReleaseTests(unittest.TestCase):
    def setUp(self):
        self.tmp=tempfile.TemporaryDirectory();self.addCleanup(self.tmp.cleanup)
        self.work=Path(self.tmp.name);self.site=self.work/'site';self.site.mkdir()
        (self.site/'.env').write_text('PUBLIC_ADDRESS=example.com\n')
        self.bin=self.work/'bin';self.bin.mkdir();self.log=self.work/'docker.log'
        fake=self.bin/'docker'
        fake.write_text('''#!/usr/bin/env python3
import json,os,sys
from pathlib import Path
log=Path(os.environ['DOCKER_CALL_LOG'])
with log.open('a') as f:f.write(json.dumps({'tag':os.environ.get('IMAGE_TAG'),'args':sys.argv[1:]})+'\\n')
if '--build' in sys.argv and os.environ.get('FAIL_NEW_RELEASE')=='1':sys.exit(1)
''');fake.chmod(0o755)
        self.env={**os.environ,'PATH':str(self.bin)+os.pathsep+os.environ['PATH'],'DOCKER_CALL_LOG':str(self.log)}
    def archive(self,name,extra=None):
        path=self.work/(name+'.tar.gz')
        with tarfile.open(path,'w:gz') as tar:
            for key,value in {'docker-compose.prod.yml':'name: taskflow\n# '+name,**(extra or {})}.items():
                data=value.encode();info=tarfile.TarInfo(key);info.size=len(data);tar.addfile(info,io.BytesIO(data))
        return path
    def run_release(self,path,**env):
        return subprocess.run(['bash',str(ROOT/'scripts/release.sh'),str(path),str(self.site)],env={**self.env,**env},capture_output=True,text=True)
    def test_success_promotes_and_preserves_shared_secret(self):
        result=self.run_release(self.archive('first'));self.assertEqual(result.returncode,0,result.stderr)
        current=(self.site/'current').resolve();self.assertEqual((current/'.env').resolve(),(self.site/'.env').resolve())
        self.assertEqual((self.site/'.env').read_text(),'PUBLIC_ADDRESS=example.com\n')
        call=json.loads(self.log.read_text().splitlines()[0]);self.assertEqual(call['tag'],current.name)
    def test_failed_update_rolls_back_without_promoting(self):
        self.assertEqual(self.run_release(self.archive('first')).returncode,0)
        first=(self.site/'current').resolve();result=self.run_release(self.archive('second'),FAIL_NEW_RELEASE='1')
        self.assertNotEqual(result.returncode,0);self.assertEqual((self.site/'current').resolve(),first)
        last=json.loads(self.log.read_text().splitlines()[-1]);self.assertEqual(last['tag'],first.name);self.assertIn('--no-build',last['args'])
    def test_new_release_does_not_retain_deleted_source(self):
        self.assertEqual(self.run_release(self.archive('first',{'obsolete.txt':'old'})).returncode,0)
        self.assertEqual(self.run_release(self.archive('second')).returncode,0)
        self.assertFalse(((self.site/'current').resolve()/'obsolete.txt').exists())
        self.assertTrue(((self.site/'previous').resolve()/'obsolete.txt').exists())
    def test_first_deployment_creates_private_shared_secrets(self):
        (self.site/'.env').unlink()
        source=self.archive('fresh',{'scripts/setup.py':(ROOT/'scripts/setup.py').read_text()})
        result=self.run_release(source)
        self.assertEqual(result.returncode,0,result.stderr)
        secret=self.site/'.env'
        self.assertEqual(secret.stat().st_mode & 0o777,0o600)
        self.assertNotIn('replace_with',secret.read_text())
        self.assertNotIn('USER_DB_PASSWORD=',result.stdout)
    def test_hostname_is_persisted_and_protocols_are_rejected(self):
        source=self.archive('host')
        result=self.run_release(source,TASKFLOW_PUBLIC_ADDRESS='taskflow.example.com')
        self.assertEqual(result.returncode,0,result.stderr)
        self.assertIn('PUBLIC_ADDRESS=taskflow.example.com',(self.site/'.env').read_text())
        result=self.run_release(source,TASKFLOW_PUBLIC_ADDRESS='https://invalid.example.com/path')
        self.assertNotEqual(result.returncode,0)
        self.assertIn('PUBLIC_ADDRESS=taskflow.example.com',(self.site/'.env').read_text())
    def test_unsafe_archive_is_rejected_before_running_docker(self):
        result=self.run_release(self.archive('unsafe',{'../outside.txt':'bad'}))
        self.assertNotEqual(result.returncode,0);self.assertFalse((self.work/'outside.txt').exists());self.assertFalse(self.log.exists())
    def test_archive_cannot_overwrite_server_secrets(self):
        result=self.run_release(self.archive('secret',{'.env':'PUBLIC_ADDRESS=attacker.com'}))
        self.assertNotEqual(result.returncode,0);self.assertEqual((self.site/'.env').read_text(),'PUBLIC_ADDRESS=example.com\n')

if __name__=='__main__':unittest.main()
