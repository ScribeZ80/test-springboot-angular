import { ComponentFixture, TestBed } from '@angular/core/testing';
import { AddTaskComponent } from './add-task.component';
import { FormsModule } from '@angular/forms';
import { TaskService } from '../../services/task.service';
import { of, throwError } from 'rxjs';
import { By } from '@angular/platform-browser';

describe('AddTaskComponent', () => {
  let fixture: ComponentFixture<AddTaskComponent>;
  let component: AddTaskComponent;
  let taskServiceSpy: jasmine.SpyObj<TaskService>;

  beforeEach(async () => {
    taskServiceSpy = jasmine.createSpyObj('TaskService', ['createTask']);
    await TestBed.configureTestingModule({
      imports: [AddTaskComponent, FormsModule],
      providers: [{ provide: TaskService, useValue: taskServiceSpy }],
    }).compileComponents();
    fixture = TestBed.createComponent(AddTaskComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should not submit if fields are empty', () => {
    component.label = '';
    component.description = '';
    spyOn(component, 'onSubmit').and.callThrough();
    component.onSubmit();
    expect(taskServiceSpy.createTask).not.toHaveBeenCalled();
  });

  it('should call createTask and reset after success', () => {
    component.label = 'Test';
    component.description = 'Desc';
    taskServiceSpy.createTask.and.returnValue(of({ id: 1, label: 'Test', description: 'Desc', completed: false }));
    spyOn(component.taskCreated, 'emit');
    component.onSubmit();
    expect(taskServiceSpy.createTask).toHaveBeenCalled();
    expect(component.label).toBe('');
    expect(component.description).toBe('');
    expect(component.loading()).toBeFalse();
    expect(component.taskCreated.emit).toHaveBeenCalled();
  });

  it('should handle error when adding', () => {
    component.label = 'Test';
    component.description = 'Desc';
    taskServiceSpy.createTask.and.returnValue(throwError(() => new Error('fail')));
    spyOn(console, 'error');
    component.onSubmit();
    expect(taskServiceSpy.createTask).toHaveBeenCalled();
    expect(component.loading()).toBeFalse();
    expect(console.error).toHaveBeenCalled();
  });

  it('should disable the button if loading', () => {
    component.label = 'Test';
    component.description = 'Desc';
    component.loading.set(true);
    fixture.detectChanges();
    const btn = fixture.debugElement.query(By.css('button[type=submit]'));
    expect(btn.nativeElement.disabled).toBeTrue();
  });
}); 