export type Status='PENDING'|'IN_PROGRESS'|'COMPLETED';
export type Priority='LOW'|'MEDIUM'|'HIGH'|'URGENT';
export interface User {id:number;name:string;email:string}
export interface Project {id:number;name:string;description:string;color:string;archived:boolean;createdAt:string}
export interface Checklist {id:string;title:string;done:boolean}
export interface Task {id:number;title:string;description:string;status:Status;priority:Priority;dueDate:string|null;createdAt:string;updatedAt:string;assignedUserId:number;projectId:number|null;version:number;labels:string[];checklist:Checklist[]}
export type TaskInput=Pick<Task,'title'|'description'|'status'|'priority'|'dueDate'|'projectId'|'labels'|'checklist'> & {version?:number};
export interface Activity {id:number;action:string;subject:string;taskId:number|null;createdAt:string}
export interface Comment {id:number;author:string;body:string;createdAt:string}
export const statuses:Status[]=['PENDING','IN_PROGRESS','COMPLETED'];
export const statusLabel:Record<Status,string>={PENDING:'To do',IN_PROGRESS:'In progress',COMPLETED:'Done'};
export const priorityLabel:Record<Priority,string>={LOW:'Low',MEDIUM:'Medium',HIGH:'High',URGENT:'Urgent'};
