import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import { TaskDetailComponent } from './task-detail.component';
import { TaskService } from '../../services/task.service';
import { ActivatedRoute } from '@angular/router';
import { of, throwError } from 'rxjs';
import { Task } from '../../models/task.model';
import { TaskStatusToggleComponent } from '../../../../shared/components';

const TASK: Task = { id: 1, label: 'Tâche 1', description: 'Desc', completed: false };

describe('TaskDetailComponent', () => {
  let fixture: ComponentFixture<TaskDetailComponent>;
  let component: TaskDetailComponent;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;
  let routeStub: any;

  beforeEach(async () => {
    taskServiceSpy = jasmine.createSpyObj('TaskService', ['fetchTaskById', 'updateTaskStatus']);
    routeStub = { snapshot: { paramMap: { get: () => '1' } } };
    taskServiceSpy.fetchTaskById.and.returnValue(of(TASK));
    await TestBed.configureTestingModule({
      imports: [TaskDetailComponent, TaskStatusToggleComponent],
      providers: [
        { provide: TaskService, useValue: taskServiceSpy },
        { provide: ActivatedRoute, useValue: routeStub },
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should display the task details', () => {
    fixture.detectChanges();
    expect(component.task()).toEqual(TASK);
  });

  it('should call updateTaskStatus on toggle', () => {
    taskServiceSpy.updateTaskStatus.and.returnValue(of({ ...TASK, completed: true }));
    component.toggleStatus(true);
    expect(taskServiceSpy.updateTaskStatus).toHaveBeenCalledWith(TASK.id, true);
    expect(component.isUpdating()).toBeFalse();
    expect(component.task()?.completed).toBeTrue();
  });

  it('should handle error on toggle', () => {
    taskServiceSpy.updateTaskStatus.and.returnValue(throwError(() => new Error('fail')));
    component.toggleStatus(true);
    expect(component.isUpdating()).toBeFalse();
  });

  it('should handle error on fetch', fakeAsync(() => {
    taskServiceSpy.fetchTaskById.and.returnValue(throwError(() => new Error('fail')));
    // Destroy and recreate the component to relaunch the effect
    fixture.destroy();
    fixture = TestBed.createComponent(TaskDetailComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
    tick();
    expect(component.isLoading()).toBeFalse();
    expect(component.task()).toBeNull();
  }));
});
