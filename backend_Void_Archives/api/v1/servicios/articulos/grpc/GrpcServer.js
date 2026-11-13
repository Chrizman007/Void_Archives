const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const fs = require('fs');
const path = require('path');
const { ArticuloRepository } = require('../src/dominio/repositorio/ArticuloRepositorio');

const PROTO_PATH = path.join(__dirname, '..', 'proto', 'media.proto');
const DIRECTORIO_IMAGENES = path.join(__dirname, '..', 'public', 'imagenes');

const packageDefinition = protoLoader.loadSync(PROTO_PATH, {
  keepCase: true,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true
});

const mediaProto = grpc.loadPackageDefinition(packageDefinition).media;

const subirImagenHandler = (call, callback) => {
  let archivoNombre = '';
  let articuloId = '';
  const fileBuffer = [];

  call.on('data', (request) => {
    if (request.articuloId) articuloId = request.articuloId;
    if (request.nombreArchivo) archivoNombre = request.nombreArchivo;
    if (request.chunk) fileBuffer.push(request.chunk);
  });

  call.on('end', async () => {
    try {
      const archivoCompleto = Buffer.concat(fileBuffer);

      if (!fs.existsSync(DIRECTORIO_IMAGENES)) {
        fs.mkdirSync(DIRECTORIO_IMAGENES, { recursive: true });
      }

      const nombreSeguro = archivoNombre.replace(/[^a-zA-Z0-9.\-_]/g, '_');
      const rutaDestino = path.join(DIRECTORIO_IMAGENES, nombreSeguro);

      fs.writeFileSync(rutaDestino, archivoCompleto);
      console.log(`[gRPC] Archivo guardado con éxito: ${nombreSeguro}`);

      if (articuloId) {
        try {
          await ArticuloRepository.actualizarImagen(articuloId, nombreSeguro);
          console.log(`[gRPC] BD actualizada con imagen: ${nombreSeguro}`);
        } catch (updateError) {
          console.warn('Error BD al actualizar imagen:', updateError.message);
        }
      }

      callback(null, {
        exito: true,
        mensaje: 'Archivo guardado.',
        urlImagen: nombreSeguro
      });
    } catch (error) {
      console.error('Fallo en gRPC:', error);
      callback({ code: grpc.status.INTERNAL, message: error.message });
    }
  });
};

const descargarImagenHandler = (call) => {
  const nombreArchivo = call.request.nombreArchivo;
  if (!nombreArchivo) {
    return call.emit('error', { code: grpc.status.INVALID_ARGUMENT, details: 'Falta nombre' });
  }

  const nombreSeguro = nombreArchivo.replace(/[^a-zA-Z0-9.\-_]/g, '_');
  const rutaCompleta = path.join(DIRECTORIO_IMAGENES, nombreSeguro);

  if (!fs.existsSync(rutaCompleta)) {
    return call.emit('error', { code: grpc.status.NOT_FOUND, details: 'Imagen no existe' });
  }

  const readStream = fs.createReadStream(rutaCompleta, { highWaterMark: 1024 * 64 });

  readStream.on('data', (chunk) => {
    if (call.cancelled) {
      readStream.destroy();
      return;
    }
    call.write({ chunk });
  });

  readStream.on('end', () => call.end());

  readStream.on('error', () => {
    call.emit('error', { code: grpc.status.INTERNAL, details: 'Error de lectura' });
  });
};

function iniciarServidorGrpc(grpcPort = 50051) {
  const grpcServer = new grpc.Server();
  grpcServer.addService(mediaProto.MediaService.service, {
    SubirImagen: subirImagenHandler,
    DescargarImagen: descargarImagenHandler
  });

  grpcServer.bindAsync(`0.0.0.0:${grpcPort}`, grpc.ServerCredentials.createInsecure(), (err, port) => {
    if (err) {
      console.error('No se pudo arrancar el servidor gRPC:', err);
      return;
    }
    grpcServer.start();
    console.log(`📸 Servidor gRPC (Multimedia) corriendo MUNDIALMENTE en el puerto: ${port}`);
  });

  return grpcServer;
}

module.exports = {
  iniciarServidorGrpc,
  subirImagenHandler,
  descargarImagenHandler
};
