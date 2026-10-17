#!/usr/bin/env python3
"""Run the real app locally with file-backed H2 databases, Java 17+, Node 20+ and Maven."""
from pathlib import Path
import os,subprocess,signal,time,sys,urllib.request
root=Path(__file__).resolve().parents[1]
env=os.environ.copy()
mac17=Path('/Library/Java/JavaVirtualMachines/temurin-17.jdk/Contents/Home')
if mac17.exists():env['JAVA_HOME']=str(mac17)
if env.get('JAVA_HOME'):env['PATH']=env['JAVA_HOME']+'/bin:'+env['PATH']
for name in ['.local-data','.local-logs']:(root/name).mkdir(exist_ok=True)
env.setdefault('TASKFLOW_DATA_DIR',str(root/'.local-data'))
children=[];logs=[]
def stop(*_):
 for p in children:
  if p.poll()is None:p.terminate()
 for p in children:
  try:p.wait(timeout=10)
  except subprocess.TimeoutExpired:p.kill()
 for f in logs:f.close()
signal.signal(signal.SIGTERM,lambda *_:sys.exit(0))
try:
 subprocess.run(['mvn','-B','-f','backend/pom.xml','-Plocal','-Dmaven.test.skip=true','clean','package'],cwd=root,env=env,check=True)
 for name,port in [('userservice',8080),('taskservice',8081),('apigateway',8090)]:
  f=open(root/'.local-logs'/f'{name}.log','w');logs.append(f)
  children.append(subprocess.Popen(['java','-jar',f'backend/{name}/target/{name}-1.0.0-SNAPSHOT.jar','--spring.profiles.active=local'],cwd=root,env=env,stdout=f,stderr=subprocess.STDOUT))
  for attempt in range(100):
   if children[-1].poll()is not None:raise RuntimeError(f'{name} stopped. See .local-logs/{name}.log')
   try:
    with urllib.request.urlopen(f'http://127.0.0.1:{port}/actuator/health',timeout=2)as response:
     if response.status==200:break
   except Exception:time.sleep(1)
  else:raise RuntimeError(f'{name} did not become healthy.')
 if not(root/'frontend/node_modules').exists():subprocess.run(['npm','ci'],cwd=root/'frontend',env=env,check=True)
 children.append(subprocess.Popen(['node','node_modules/vite/bin/vite.js','--host','127.0.0.1','--strictPort'],cwd=root/'frontend',env=env))
 print('\nTaskFlow is running at http://127.0.0.1:5173 — press Ctrl+C to stop.\n',flush=True)
 while all(p.poll()is None for p in children):time.sleep(1)
except KeyboardInterrupt:pass
finally:stop()
