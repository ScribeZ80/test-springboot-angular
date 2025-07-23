import { ComponentFixture, TestBed } from '@angular/core/testing';
import { TaskStatusToggleComponent } from './task-status-toggle.component';
import { By } from '@angular/platform-browser';
import { Component } from '@angular/core';

// Wrapper to test @input/@output
@Component({
  imports: [
    TaskStatusToggleComponent
  ],
  template: `
    <app-task-status-toggle [isCompleted]="isCompleted" [isUpdating]="isUpdating" [size]="size"
                            (statusChange)="onStatusChange($event)"></app-task-status-toggle>`
})
class WrapperComponent {
  isCompleted = false;
  isUpdating = false;
  size: 'small' | 'medium' | 'large' = 'medium';
  statusChangedValue: boolean | null = null;
  onStatusChange(val: boolean) { this.statusChangedValue = val; }
}

describe('TaskStatusToggleComponent', () => {
  let fixture: ComponentFixture<WrapperComponent>;
  let wrapper: WrapperComponent;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TaskStatusToggleComponent, WrapperComponent],
    }).compileComponents();
    fixture = TestBed.createComponent(WrapperComponent);
    wrapper = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should display the correct text according to status', () => {
    wrapper.isCompleted = false;
    fixture.detectChanges();
    let btn = fixture.debugElement.query(By.css('button'));
    expect(btn.nativeElement.textContent).toContain('Marquer comme terminée');
    wrapper.isCompleted = true;
    fixture.detectChanges();
    btn = fixture.debugElement.query(By.css('button'));
    expect(btn.nativeElement.textContent).toContain('Marquer comme en cours');
  });

  it('should emit statusChange on click if not updating', () => {
    wrapper.isCompleted = false;
    wrapper.isUpdating = false;
    fixture.detectChanges();
    const btn = fixture.debugElement.query(By.css('button'));
    btn.nativeElement.click();
    expect(wrapper.statusChangedValue).toBe(true);
  });

  it('should not emit statusChange if updating', () => {
    wrapper.isCompleted = false;
    wrapper.isUpdating = true;
    fixture.detectChanges();
    const btn = fixture.debugElement.query(By.css('button'));
    btn.nativeElement.click();
    expect(wrapper.statusChangedValue).toBeNull();
  });

  it('should disable the button if updating', () => {
    wrapper.isUpdating = true;
    fixture.detectChanges();
    const btn = fixture.debugElement.query(By.css('button'));
    expect(btn.nativeElement.disabled).toBeTrue();
  });

  it('should have a correct aria-label', () => {
    wrapper.isCompleted = false;
    wrapper.isUpdating = false;
    fixture.detectChanges();
    let btn = fixture.debugElement.query(By.css('button'));
    expect(btn.attributes['aria-label']).toContain('marquer comme terminée');
    wrapper.isCompleted = true;
    fixture.detectChanges();
    btn = fixture.debugElement.query(By.css('button'));
    expect(btn.attributes['aria-label']).toContain('marquer comme en cours');
  });
});
