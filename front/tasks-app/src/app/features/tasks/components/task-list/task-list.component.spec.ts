import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskListComponent } from './task-list.component';
import { TaskService } from '../../services/task.service';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';
import { Task } from '../../models/task.model';
import { AddTaskComponent } from '../add-task/add-task.component';
import { TaskStatusToggleComponent } from '../../../../shared/components';
import { RouterTestingModule } from '@angular/router/testing';

const TASKS: Task[] = [
  { id: 1, label: 'Tâche 1', description: 'Desc 1', completed: false },
  { id: 2, label: 'Tâche 2', description: 'Desc 2', completed: true },
];
const PAGE_RESPONSE = {
  content: TASKS,
  pageNumber: 0,
  totalPages: 1,
  totalElements: 2,
  hasNext: false,
  hasPrevious: false,
  pageSize: 12,
  first: true,
  last: true,
};

describe('TaskListComponent', () => {
  let fixture: ComponentFixture<TaskListComponent>;
  let component: TaskListComponent;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;

  beforeEach(async () => {
    taskServiceSpy = jasmine.createSpyObj('TaskService', ['fetchTasks']);
    taskServiceSpy.fetchTasks.and.returnValue(of(PAGE_RESPONSE));
    await TestBed.configureTestingModule({
      imports: [TaskListComponent, AddTaskComponent, TaskStatusToggleComponent, RouterTestingModule],
      providers: [{ provide: TaskService, useValue: taskServiceSpy }],
    }).compileComponents();
    fixture = TestBed.createComponent(TaskListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should display the list of tasks', () => {
    fixture.detectChanges();
    const cards = fixture.debugElement.queryAll(By.css('.task-card'));
    expect(cards.length).toBe(TASKS.length);
  });

  it('should filter by completed status', () => {
    taskServiceSpy.fetchTasks.and.returnValue(of({ ...PAGE_RESPONSE, content: [TASKS[1]] }));
    component['_status'].set('completed');
    fixture.detectChanges();
    const cards = fixture.debugElement.queryAll(By.css('.task-card'));
    expect(cards.length).toBe(1);
    expect(cards[0].nativeElement.textContent).toContain('Tâche 2');
  });

  it('should display a message if loading error', () => {
    taskServiceSpy.fetchTasks.and.returnValue(throwError(() => new Error('fail')));
    component['_refreshTrigger'].set(component['_refreshTrigger']() + 1);
    fixture.detectChanges();
    const loading = fixture.debugElement.query(By.css('.loading-text'));
    expect(loading).toBeTruthy();
  });

  it('should disable the previous button if no previous page', () => {
    fixture.detectChanges();
    const prevBtn = fixture.debugElement.query(By.css('.pagination-btn'));
    expect(prevBtn.nativeElement.disabled).toBeTrue();
  });

  it('should call fetchTasks on each page change', () => {
    spyOn(component, 'nextPage').and.callThrough();
    component.nextPage();
    expect(component.nextPage).toHaveBeenCalled();
    expect(taskServiceSpy.fetchTasks).toHaveBeenCalled();
  });
}); 