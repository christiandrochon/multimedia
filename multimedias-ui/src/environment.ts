export const environment = {
  production: false,
  apiBaseUrl: window.location.hostname !== 'localhost' ? 'http://multimedias-server:8087' : 'http://localhost:8087',
  imagesBaseUrl: 'http://localhost:8087/images',
};
