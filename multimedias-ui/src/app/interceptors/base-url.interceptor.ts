import { HttpInterceptorFn } from '@angular/common/http';
import {environment} from '../../environment.prod';

/**
 * Fonction intercepteur HTTP pour ajouter automatiquement une URL de base
 * à toutes les requêtes relatives.
 */
export const BaseUrlInterceptor: HttpInterceptorFn = (req, next) => {
  // Détection dynamique : si l'application tourne en local
  const isDocker = window.location.hostname !== 'localhost';

  // URLs dynamiques en fonction de l'environnement
  const apiUrl = isDocker ? 'http://multimedias-server:8087' : 'http://localhost:8087';
  const imagesUrl = isDocker ? 'http://multimedias-server:8087/images' : 'http://localhost:8087/images';

  // Log l'URL avant de l'envoyer
  console.log('Original request URL:', req.url);

  // Ne pas modifier les URLs absolues
  if (req.url.startsWith('http')) {
    console.log('Absolute URL detected, skipping base URL:', req.url);
    return next(req);
  }

  // Ajouter la base URL pour les URLs relatives
  const updatedRequest = req.clone({
    url: `${apiUrl}${req.url}`,
  });

  console.log('Updated request URL:', updatedRequest.url);
  return next(updatedRequest);
};

