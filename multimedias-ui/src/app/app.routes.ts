import {provideRouter, Routes} from '@angular/router';

import {MultimediaComponent} from './multimedia/multimedia.component'; // Chemin correct pour MultimediaComponent
import {HelloComponent} from './hello/hello.component';
import {CouleursComponent} from './couleurs/couleurs.component';
import {HomeComponent} from './home/home.component';
import {GreyHistogramComponent} from './histogramme/grey-histogram/grey-histogram.component';
import {ColorHistogramComponent} from './histogramme/color-histogram/color-histogram.component';
import {DescripteurComponent} from './descripteur/descripteur/descripteur.component';
import {SimilariteComponent} from './similaritecouleur/similarite.component';
import {ResnetComponent} from './vt/resnet.component';
import {ResnetseulComponent} from './resnetseul/resnetseul.component';
import {SimilaritetextureComponent} from './similaritetexture/similaritetexture.component';
import {SimilaritecouleurtextureComponent} from './similaritecouleurtexture/similaritecouleurtexture.component';
import {SimilariteformeComponent} from './similariteforme/similariteforme.component';
import {SimilariteglobaleComponent} from './similariteglobale/similariteglobale.component';
import {PrecisionRecallComponent} from './precision-recall/precision-recall.component';
import {PrecisionrappelgenerationComponent} from './precisionrappelgeneration/precisionrappelgeneration.component';


export const appRoutes: Routes = [
  {path: '', component: HomeComponent},
  {path: 'images', component: MultimediaComponent},
  {path: 'hello', component: HelloComponent},
  {path: 'createCheckerboard', component: CouleursComponent},
  {path: 'swapRandomImage', component: CouleursComponent},
  {path: 'histogramme-gris/:bins', component: GreyHistogramComponent},
  {path: 'histogramme-couleur/:bins', component: ColorHistogramComponent},
  {path: 'descripteurs', component: DescripteurComponent},
  {path: 'similaritecouleur', component: SimilariteComponent},
  {path: 'resnetclasses', component: ResnetseulComponent},
  // {path: 'resnetclasses/similar', component: ResnetComponent},
  {path: 'VTclasses/similar', component: ResnetComponent},
  {path: 'evaluation', component: PrecisionRecallComponent},
  {path: 'similaritetexture', component: SimilaritetextureComponent},
  {path: 'similaritecouleurtexture', component: SimilaritecouleurtextureComponent},
  {path: 'similariteforme', component: SimilariteformeComponent},
  {path: 'similariteglobale', component: SimilariteglobaleComponent},
  {path: 'precision-rappel', component: PrecisionrappelgenerationComponent},
];

export const appRoutingProviders = [
  provideRouter(appRoutes)
];
