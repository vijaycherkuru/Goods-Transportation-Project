import { Routes } from '@angular/router';
import { SignUp } from './sign-up/sign-up';
import { Login } from './login/login';
import { Dashboard } from './dashboard/dashboard';
import { Profile } from './profile/profile';
import { CreateRide } from './create-ride/create-ride';
import { SearchRide } from './search-ride/search-ride';
import { HomepageComponent } from './components/homepage';
import { MainLayout } from './main-layout/main-layout';
import { UserDashboard } from './user-dashboard/user-dashboard';
// Import other components as you build them

export const routes: Routes = [
  { path: '', component: HomepageComponent },
  { path: 'userDashboard', component: UserDashboard },

  {
    path: '',
    component: MainLayout,
    children: [
      { path: 'dashboard', component: Dashboard },
      { path: 'profile', component: Profile },
      { path: 'create-ride', component: CreateRide },
      {
        path: 'tracking',
        loadComponent: () => import('./tracking/tracking').then(m => m.TrackingComponent)
      },
      {
        path: 'your-rides',
        loadComponent: () => import('./your-rides/your-rides').then(m => m.YourRides)
      },
      {
        path: 'your-orders',
        loadComponent: () => import('./your-orders/your-orders').then(m => m.YourOrders)
      },
      {
        path: 'search-ride',
        loadComponent: () => import('./search-ride/search-ride').then(m => m.SearchRide)
      },
      // Add more routes as you build more components:
      // { path: 'settings', component: Settings },
      // { path: 'requests', component: Requests },
    ]
  },
  { path: 'login', component: Login },
  { path: 'sign-up', component: SignUp }
];
