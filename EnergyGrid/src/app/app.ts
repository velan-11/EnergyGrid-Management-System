import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ToastsContainerComponent } from './components/toast/toasts-container.component';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, ToastsContainerComponent],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  title = 'energygrid-frontend';
}
