(window as any).global = window;
import { bootstrapApplication } from '@angular/platform-browser';
import { AppComponent } from './app/app';

bootstrapApplication(AppComponent)
  .catch(err => console.error(err));
