import { Routes } from '@angular/router';
import { ContentComponent } from './pages/content/content.component';
import { AuthGuard } from './guard/auth-guard';

export const routes: Routes = [
    { path: '', component: ContentComponent, canActivate: [AuthGuard]},
    { path: '**', redirectTo: '' }
];
