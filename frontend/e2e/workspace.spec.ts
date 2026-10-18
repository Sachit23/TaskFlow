import {test,expect,type Page} from '@playwright/test';
async function signup(page:Page){
 const email=`alex-${Date.now()}-${Math.random().toString(16).slice(2)}@example.com`;
 await page.goto('/');await page.getByRole('button',{name:'Create an account'}).click();
 await page.getByLabel('Full name').fill('Alex Morgan');await page.getByLabel('Email address').fill(email);await page.getByLabel('Password',{exact:true}).fill('A-secure-password123');
 await page.getByRole('button',{name:'Create your workspace'}).click();await expect(page.getByRole('heading',{name:/Good (morning|afternoon|evening), Alex/})).toBeVisible();
 return email;
}
test('complete real workspace lifecycle on desktop',async({page},info)=>{
 const errors:string[]=[];page.on('pageerror',e=>errors.push(e.message));
 const email=await signup(page);await page.getByRole('button',{name:'Create project',exact:true}).click();
 let dialog=page.getByRole('dialog');await dialog.getByLabel('Project name').fill('Product launch');await dialog.getByLabel('Description').fill('A thoughtful launch, one step at a time.');await dialog.getByRole('button',{name:'Create project',exact:true}).click();await expect(dialog).not.toBeVisible();
 await page.getByRole('button',{name:/^New task\b/}).click();dialog=page.getByRole('dialog');await dialog.getByLabel('Task title').fill('Design the launch experience');await dialog.getByLabel('Description').fill('Map the journey and create an experience people will remember.');await dialog.getByRole('combobox',{name:'Project',exact:true}).selectOption({label:'Product launch'});await dialog.getByLabel('Priority').selectOption('HIGH');await dialog.getByLabel('Labels').fill('Design, Launch');await dialog.getByLabel('New subtask').fill('Map the user journey');await dialog.getByRole('button',{name:'Add',exact:true}).click();await dialog.getByRole('button',{name:'Create task',exact:true}).click();await expect(dialog).not.toBeVisible();
 await page.getByRole('button',{name:'My tasks',exact:false}).first().click();await expect(page.getByRole('heading',{name:'Design the launch experience'})).toBeVisible();await page.getByRole('heading',{name:'Design the launch experience'}).click();dialog=page.getByRole('dialog');await dialog.getByRole('combobox',{name:'Status',exact:true}).selectOption('IN_PROGRESS');await dialog.getByLabel('Complete Map the user journey').check();await dialog.getByRole('button',{name:'Save changes'}).click();await expect(dialog).not.toBeVisible();
 await page.getByRole('heading',{name:'Design the launch experience'}).click();dialog=page.getByRole('dialog');await dialog.getByLabel('Write a note').fill('The first direction is ready for review.');await dialog.getByRole('button',{name:'Post note'}).click();await expect(dialog.getByText('The first direction is ready for review.')).toBeVisible();await dialog.getByRole('button',{name:'Close dialog'}).click();
 await page.getByRole('button',{name:'List',exact:true}).click();await expect(page.getByRole('cell',{name:'Product launch'})).toBeVisible();await page.getByRole('combobox',{name:'Status for Design the launch experience'}).selectOption('COMPLETED');await expect(page.getByRole('combobox',{name:'Status for Design the launch experience'})).toHaveValue('COMPLETED');
 await page.getByRole('button',{name:'Overview',exact:true}).click();await page.screenshot({path:info.outputPath('desktop-overview.png'),fullPage:true});
 await page.getByRole('button',{name:'Settings',exact:true}).click();await page.getByLabel('Full name').fill('Alex Taylor');await page.getByRole('button',{name:'Save profile'}).click();await expect(page.getByText('Profile updated.')).toBeVisible();await page.getByRole('button',{name:'Sign out',exact:true}).click();await expect(page.getByRole('heading',{name:'Welcome back.'})).toBeVisible();
 await page.getByLabel('Email address').fill(email);await page.getByLabel('Password',{exact:true}).fill('A-secure-password123');await page.getByRole('button',{name:'Sign in to your workspace'}).click();await expect(page.getByRole('heading',{name:/Good (morning|afternoon|evening), Alex/})).toBeVisible();await expect(page.getByRole('heading',{name:'Product launch'})).toBeVisible();
 expect(errors).toEqual([]);
});
test('sample workspace, search, board drag, CSV export and task deletion',async({page},info)=>{
 await signup(page);await page.getByRole('button',{name:'Try a sample project'}).click();await expect(page.getByRole('heading',{name:'Your next big thing'})).toBeVisible();
 await page.getByRole('button',{name:'My tasks',exact:false}).first().click();const source=page.getByRole('article').filter({hasText:'Make the first version tangible'});const done=page.locator('.board-column').filter({has:page.getByRole('heading',{name:'Done',exact:true})});await source.dragTo(done);await expect(done.getByRole('heading',{name:'Make the first version tangible'})).toBeVisible();
 await page.getByLabel('Search tasks').fill('promising');await expect(page.getByRole('heading',{name:'Explore a few promising directions'})).toBeVisible();await expect(page.getByRole('heading',{name:'Make the first version tangible'})).toHaveCount(0);
 const download=page.waitForEvent('download');await page.getByRole('button',{name:'Export filtered tasks as CSV'}).click();expect((await download).suggestedFilename()).toMatch(/taskflow-.*\.csv/);await page.getByLabel('Search tasks').fill('');
 await page.screenshot({path:info.outputPath('desktop-board.png'),fullPage:true});
 await page.getByRole('heading',{name:'Explore a few promising directions'}).click();const dialog=page.getByRole('dialog');await dialog.getByRole('button',{name:'Delete task',exact:true}).click();await dialog.getByRole('button',{name:'Confirm deletion'}).click();await expect(dialog).not.toBeVisible();await expect(page.getByRole('heading',{name:'Explore a few promising directions'})).toHaveCount(0);
});
test('mobile navigation and accessible task creation in dark mode',async({page},info)=>{
 await page.setViewportSize({width:390,height:844});await signup(page);
 await page.getByRole('button',{name:'Open navigation'}).click();await page.getByRole('button',{name:'Use dark theme'}).click();await page.getByRole('button',{name:'My tasks',exact:false}).first().click();
 await page.getByRole('button',{name:/^New task\b/}).click();const dialog=page.getByRole('dialog');await dialog.getByLabel('Task title').fill('A focused mobile task');await dialog.getByRole('button',{name:'Create task',exact:true}).click();await expect(dialog).not.toBeVisible();await expect(page.getByRole('heading',{name:'A focused mobile task'})).toBeVisible();
 await page.screenshot({path:info.outputPath('mobile-dark-board.png'),fullPage:true});expect(await page.evaluate(()=>document.documentElement.scrollWidth<=innerWidth)).toBe(true);
});
test('two accounts remain isolated through the gateway',async({request})=>{
 const create=async()=>{const r=await request.post('/api/users/register',{data:{name:'Isolation Test',email:`isolation-${Date.now()}-${Math.random()}@example.com`,password:'A-secure-password123'}});expect(r.status()).toBe(201);return (await r.json()).token as string;};
 const a=await create(),b=await create();const r=await request.post('/api/tasks',{headers:{Authorization:'Bearer '+a},data:{title:'Private work',priority:'HIGH'}});expect(r.status()).toBe(201);const task=await r.json();
 expect((await request.get('/api/tasks/'+task.id,{headers:{Authorization:'Bearer '+b}})).status()).toBe(404);
 expect(await(await request.get('/api/tasks',{headers:{Authorization:'Bearer '+b}})).json()).toEqual([]);
 expect((await request.get('/api/tasks')).status()).toBe(401);
});
