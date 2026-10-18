let token=sessionStorage.getItem('taskflow.token');
export function setToken(value:string|null){token=value;value?sessionStorage.setItem('taskflow.token',value):sessionStorage.removeItem('taskflow.token');}
export function hasSession(){return !!token;}
export class ApiError extends Error {constructor(public status:number,message:string){super(message)}}
export async function api<T>(path:string,options:RequestInit={}):Promise<T>{
 const response=await fetch('/api'+path,{...options,headers:{...(options.body?{'Content-Type':'application/json'}:{}),...(token?{Authorization:`Bearer ${token}`} : {}),...options.headers}});
 if(!response.ok){const data=await response.json().catch(()=>({}));if(response.status===401&&token&&!path.includes('/login'))window.dispatchEvent(new Event('taskflow:unauthorized'));throw new ApiError(response.status,data.message|| (response.status===429?'Too many attempts. Please wait a minute.':'Something went wrong. Please try again.'));}
 return response.status===204?undefined as T:response.json();
}
export const json=(method:string,body:unknown):RequestInit=>({method,body:JSON.stringify(body)});
