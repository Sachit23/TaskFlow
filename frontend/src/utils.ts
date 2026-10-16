import type {Task} from './types';
export function today(){const d=new Date();return `${d.getFullYear()}-${String(d.getMonth()+1).padStart(2,'0')}-${String(d.getDate()).padStart(2,'0')}`;}
export function dayLabel(value:string|null){if(!value)return '';if(value===today())return 'Today';return new Date(value+'T12:00:00').toLocaleDateString(undefined,{month:'short',day:'numeric'});}
export function isOverdue(t:Task){return !!t.dueDate&&t.dueDate<today()&&t.status!=='COMPLETED';}
export function initials(name:string){return name.trim().split(/\s+/).slice(0,2).map(n=>n[0]).join('').toUpperCase();}
export function relative(value:string){const elapsed=Math.max(0,Date.now()-new Date(value+'Z').getTime());if(elapsed<60000)return 'Just now';if(elapsed<3600000)return `${Math.floor(elapsed/60000)}m ago`;if(elapsed<86400000)return `${Math.floor(elapsed/3600000)}h ago`;return new Date(value+'Z').toLocaleDateString(undefined,{month:'short',day:'numeric'});}
export function errorMessage(e:unknown){return e instanceof Error?e.message:'Something went wrong. Please try again.';}
export function exportTasks(tasks:Task[],projects:{id:number;name:string}[]){
 const rows=[['Title','Description','Status','Priority','Due date','Project','Labels'],...tasks.map(t=>[t.title,t.description,t.status,t.priority,t.dueDate||'',projects.find(p=>p.id===t.projectId)?.name||'',t.labels.join(', ')])];
 const csv=rows.map(row=>row.map(v=>'"'+String(v||'').replace(/^[=+@\-]/,"'$&").replaceAll('"','""')+'"').join(',')).join('\r\n');
 const url=URL.createObjectURL(new Blob(['\ufeff'+csv],{type:'text/csv;charset=utf-8;'}));const a=document.createElement('a');a.href=url;a.download=`taskflow-${today()}.csv`;a.click();URL.revokeObjectURL(url);
}
