import { TestBed } from '@angular/core/testing';
import { of, Subject, throwError } from 'rxjs';
import type { CustomerComment } from '../models/customer.models';
import { CustomersApiService } from '../services/customers-api.service';
import { CustomerCommentComponent } from './customer-comment.component';

describe('CustomerCommentComponent', () => {
  const getComment = vi.fn((id: string) => of<CustomerComment>({ customerId: id, comment: 'Note existante' }));
  const updateComment = vi.fn((id: string, comment: string | null) => of<CustomerComment>({ customerId: id, comment }));

  beforeEach(() => {
    getComment.mockReset().mockImplementation((id) => of({ customerId: id, comment: 'Note existante' }));
    updateComment.mockReset().mockImplementation((id, comment) => of({ customerId: id, comment }));
    TestBed.configureTestingModule({ imports: [CustomerCommentComponent], providers: [{ provide: CustomersApiService, useValue: { getComment, updateComment } }] });
  });

  const setup = () => {
    const fixture = TestBed.createComponent(CustomerCommentComponent);
    fixture.componentRef.setInput('customerId', 'client-1');
    fixture.detectChanges();
    return fixture;
  };

  it('saves a normalized changed comment on blur without a save button and shows confirmation', () => {
    const fixture = setup();
    const component = fixture.componentInstance;
    expect(getComment).toHaveBeenCalledWith('client-1');
    expect(component.form.controls.comment.value).toBe('Note existante');
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    expect(fixture.nativeElement.querySelector('button[type="submit"]')).toBeNull();
    component.form.controls.comment.setValue('  Nouvelle note\nDeuxième ligne  ');
    expect(updateComment).not.toHaveBeenCalled();
    textarea.dispatchEvent(new FocusEvent('blur'));
    fixture.detectChanges();
    expect(updateComment).toHaveBeenCalledWith('client-1', 'Nouvelle note\nDeuxième ligne');
    expect(fixture.nativeElement.textContent).toContain('Commentaire enregistré.');
    component.form.controls.comment.setValue('   ');
    textarea.dispatchEvent(new FocusEvent('blur'));
    expect(updateComment).toHaveBeenLastCalledWith('client-1', null);
  });

  it('does not save unchanged, reverted or whitespace-only changes and does not repeat a successful save', () => {
    const fixture = setup();
    const control = fixture.componentInstance.form.controls.comment;
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    textarea.dispatchEvent(new FocusEvent('blur'));
    control.setValue('Note temporaire');
    control.setValue('Note existante');
    textarea.dispatchEvent(new FocusEvent('blur'));
    control.setValue('  Note existante  ');
    textarea.dispatchEvent(new FocusEvent('blur'));
    expect(updateComment).not.toHaveBeenCalled();

    control.setValue('Nouvelle note');
    textarea.dispatchEvent(new FocusEvent('blur'));
    textarea.dispatchEvent(new FocusEvent('blur'));
    expect(updateComment).toHaveBeenCalledExactlyOnceWith('client-1', 'Nouvelle note');
  });

  it('does not send an empty unchanged comment when the API returned no comment', () => {
    getComment.mockReturnValue(of({ customerId: 'client-1', comment: null }));
    const fixture = setup();
    fixture.componentInstance.form.controls.comment.setValue('   ');
    fixture.nativeElement.querySelector('textarea').dispatchEvent(new FocusEvent('blur'));
    expect(updateComment).not.toHaveBeenCalled();
  });

  it('retries the changed comment on the next blur after a failed automatic save', () => {
    updateComment.mockReturnValueOnce(throwError(() => new Error('API error')));
    const fixture = setup();
    const component = fixture.componentInstance;
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    component.form.controls.comment.setValue('Note a conserver');
    textarea.dispatchEvent(new FocusEvent('blur'));
    expect(component.error()).toContain('Enregistrement');
    expect(component.form.controls.comment.value).toBe('Note a conserver');
    textarea.dispatchEvent(new FocusEvent('blur'));
    expect(updateComment).toHaveBeenCalledTimes(2);
    expect(component.notice()).toContain('enregistré');
    textarea.dispatchEvent(new FocusEvent('blur'));
    expect(updateComment).toHaveBeenCalledTimes(2);
  });

  it('lets an automatic save finish when closing the comment without sending it twice', () => {
    const pending = new Subject<CustomerComment>();
    updateComment.mockReturnValueOnce(pending);
    const fixture = setup();
    const component = fixture.componentInstance;
    component.form.controls.comment.setValue('Note avant fermeture');
    const textarea = fixture.nativeElement.querySelector('textarea') as HTMLTextAreaElement;
    textarea.dispatchEvent(new FocusEvent('blur'));
    textarea.dispatchEvent(new FocusEvent('blur'));
    expect(updateComment).toHaveBeenCalledExactlyOnceWith('client-1', 'Note avant fermeture');
    fixture.destroy();
    expect(pending.observed).toBe(true);
    pending.next({ customerId: 'client-1', comment: 'Note avant fermeture' });
    pending.complete();
    expect(component.notice()).toBe('');
  });

  it('keeps the entered text if saving fails and rejects overlong input', () => {
    const fixture = setup();
    const component = fixture.componentInstance;
    updateComment.mockReturnValue(throwError(() => new Error('API error')));
    component.form.controls.comment.setValue('Note à conserver');
    component.save();
    expect(component.error()).toContain('Enregistrement');
    expect(component.notice()).toBe('');
    expect(component.form.controls.comment.value).toBe('Note à conserver');
    updateComment.mockClear();
    component.form.controls.comment.setValue('a'.repeat(2001));
    component.save();
    expect(updateComment).not.toHaveBeenCalled();
  });

  it('does not invent an empty saved comment when loading fails', () => {
    getComment.mockReturnValue(throwError(() => new Error('API error')));
    const fixture = setup();
    fixture.componentInstance.save();
    expect(fixture.componentInstance.loadFailed()).toBe(true);
    expect(updateComment).not.toHaveBeenCalled();
  });

  it('cancels stale loads when switching clients', () => {
    const old = new Subject<CustomerComment>();
    getComment.mockReturnValueOnce(old);
    const fixture = setup();
    fixture.componentRef.setInput('customerId', 'client-2');
    fixture.detectChanges();
    old.next({ customerId: 'client-1', comment: 'Mauvais client' });
    expect(fixture.componentInstance.form.controls.comment.value).toBe('Note existante');
    fixture.componentInstance.form.controls.comment.setValue('Note client 2');
    fixture.componentInstance.save();
    expect(updateComment).toHaveBeenCalledWith('client-2', 'Note client 2');
  });

  it('does not show another client save response after switching clients', () => {
    const pending = new Subject<CustomerComment>();
    updateComment.mockReturnValueOnce(pending);
    const fixture = setup();
    const component = fixture.componentInstance;
    component.form.controls.comment.setValue('Note du premier client');
    component.save();
    fixture.componentRef.setInput('customerId', 'client-2');
    fixture.detectChanges();
    pending.next({ customerId: 'client-1', comment: 'Note du premier client' });
    pending.complete();
    expect(component.form.controls.comment.value).toBe('Note existante');
    expect(component.notice()).toBe('');
    expect(component.saving()).toBe(false);
  });
});
