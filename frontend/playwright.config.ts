import {defineConfig} from '@playwright/test';
export default defineConfig({
 testDir:'./e2e',fullyParallel:false,workers:1,timeout:60000,
 expect:{timeout:10000},retries:process.env.CI?1:0,
 use:{baseURL:process.env.TASKFLOW_BASE_URL||'http://127.0.0.1:5173',headless:true,trace:'retain-on-failure',screenshot:'only-on-failure',launchOptions:{executablePath:process.env.PLAYWRIGHT_CHROMIUM_EXECUTABLE_PATH}},
 webServer:process.env.TASKFLOW_BASE_URL?undefined:{gracefulShutdown:{signal:'SIGTERM',timeout:45000},command:'python3 ../scripts/dev.py',url:'http://127.0.0.1:5173',timeout:240000,reuseExistingServer:!process.env.CI},
 reporter:[['list'],['html',{open:'never'}]]
});
