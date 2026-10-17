#!/usr/bin/env python3
"""Exercise a deployed stack through its public HTTP entry point. Uses a unique account."""
import argparse,json,sys,time,uuid,urllib.request,urllib.error
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('url',nargs='?',default='http://localhost:8088')
parser.add_argument('--wait-seconds',type=int,default=0,help='Wait for the HTTPS entry point before testing writes.')
args=parser.parse_args()
base=args.url.rstrip('/')
deadline=time.monotonic()+args.wait_seconds
while True:
 try:
  with urllib.request.urlopen(base+'/healthz',timeout=5) as health:
   if health.status==200:break
 except (urllib.error.URLError,TimeoutError):
  if time.monotonic()>=deadline:raise SystemExit('The web entry point is not ready: '+base)
  time.sleep(2)
token=None
def request(path,method='GET',body=None,expected=200):
 data=None if body is None else json.dumps(body).encode()
 headers={'Content-Type':'application/json'}
 if token:headers['Authorization']='Bearer '+token
 req=urllib.request.Request(base+'/api'+path,data=data,headers=headers,method=method)
 try:
  with urllib.request.urlopen(req,timeout=30)as r:
   assert r.status==expected,(path,r.status);return json.loads(r.read())if r.status!=204 else None
 except urllib.error.HTTPError as e:
  if e.code==expected:return None
  raise RuntimeError(f'{method} {path}: HTTP {e.code}')from None
request('/tasks',expected=401)
a=request('/users/register','POST',{'name':'Smoke Test','email':f'smoke-{uuid.uuid4()}@example.com','password':str(uuid.uuid4())+'!'},201)
token=a['token'];assert 'password'not in a['user']
p=request('/projects','POST',{'name':'Deployment verification','description':'Smoke test','color':'#0c6b58','archived':False},201)
t=request('/tasks','POST',{'title':'Verify the deployed stack','projectId':p['id'],'priority':'HIGH'},201)
t['status']='COMPLETED';t=request('/tasks/'+str(t['id']),'PUT',t)
assert t['status']=='COMPLETED'
request('/tasks/'+str(t['id'])+'/comments','POST',{'body':'Verified through the public entry point.'},201)
request('/tasks/'+str(t['id']),'DELETE',expected=204)
request('/projects/'+str(p['id']),'DELETE',expected=204)
request('/users/logout','POST',expected=204)
request('/tasks',expected=401)
print('PASS: registration, authentication, project/task CRUD, comments, session revocation. The smoke-test account remains without tasks or projects.')
