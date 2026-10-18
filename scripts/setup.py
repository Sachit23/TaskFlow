#!/usr/bin/env python3
"""Create local deployment secrets without overwriting an existing environment."""
from pathlib import Path
import os,secrets
root=Path(__file__).resolve().parents[1]
path=root/'.env'
if path.exists():
 print('.env already exists; left unchanged.')
else:
 content='USER_DB_USERNAME=taskflow\nUSER_DB_PASSWORD='+secrets.token_urlsafe(36)+'\nTASK_DB_USERNAME=taskflow\nTASK_DB_PASSWORD='+secrets.token_urlsafe(36)+'\nWEB_PORT=8088\nPUBLIC_ADDRESS=taskflow.example.com\n'
 fd=os.open(path,os.O_WRONLY|os.O_CREAT|os.O_EXCL,0o600)
 with os.fdopen(fd,'w')as f:f.write(content)
 print('Created .env with unique database passwords. No secrets were printed.')
