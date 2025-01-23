import {ApplicationConfig, provideZoneChangeDetection} from '@angular/core';
import {provideRouter} from '@angular/router';

import {appRoutes} from './app.routes';
import {provideHttpClient, withInterceptors} from '@angular/common/http';
import {BaseUrlInterceptor} from './interceptors/base-url.interceptor';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({eventCoalescing: true}),
    provideRouter(appRoutes),
    provideHttpClient(
      withInterceptors([BaseUrlInterceptor])
    )
  ]
};
