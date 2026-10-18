import {describe,it,expect} from 'vitest';
import {initials,today,isOverdue,dayLabel} from './utils';
import type {Task} from './types';
describe('workspace date and identity behavior',()=>{
 it('uses initials from separate name parts',()=>{expect(initials('Alex Morgan')).toBe('AM');expect(initials('  Alex   Morgan  ')).toBe('AM')});
 it('only flags unfinished past-due tasks',()=>{expect(isOverdue({dueDate:'2000-01-01',status:'PENDING'} as Task)).toBe(true);expect(isOverdue({dueDate:'2000-01-01',status:'COMPLETED'} as Task)).toBe(false);expect(isOverdue({dueDate:null,status:'PENDING'} as Task)).toBe(false)});
 it('recognizes the current local calendar day',()=>{expect(today()).toMatch(/^\d{4}-\d{2}-\d{2}$/);expect(dayLabel(today())).toBe('Today')});
});
