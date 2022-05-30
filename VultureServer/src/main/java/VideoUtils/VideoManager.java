package VideoUtils;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Esta clase es un codificador de vídeo MPEG-4 (mp4).
 * Es capaz de abrir un fichero e ir codificando frames mientras esté abierto
 * conformando una secuencia de vídeo.
 */
public class VideoManager {

    //frames por segundo a los que codifica el vídeo
    private final int FPS = 3;

    //obje de output del fichero de vídeo
    private SeekableByteChannel out;

    //codificador de fotogramas
    private AWTSequenceEncoder encoder;

    private boolean isEncoding = false;

    private String actualPathEndoding;

    public VideoManager() {
    }

    //se inicia la codificación
    public boolean startMp4Encode(String path) throws IOException {

        //solo se puede iniciar una nueva codificación en curso si no hay una uya existente
        if (!isEncoding) {

            actualPathEndoding = path;

            //crea un objeto que gestiona el fichero abierto en el que se ván a ir escribiendo lso frames codificados.
            out = NIOUtils.writableFileChannel(path);
            //le da controlador del fichero a el codificador para que este se encarge de gestionarlo.
            encoder = new AWTSequenceEncoder(out, Rational.R(FPS, 1));
            isEncoding = true;

            return true;

        }

        return false;

    }

    //añade un frame al vídeo que se está codificando en este momento.
    public void nextFrame(byte[] image) throws IOException {

        if (isEncoding) encoder.encodeImage(toBufferedImage(image));

    }

    //termina la codificación y cierra el fichero.
    public void stopAndSave() {

        try {
            encoder.finish();
        } catch (IOException e) {
            e.printStackTrace();
        }

        NIOUtils.closeQuietly(out);

        isEncoding = false;

    }

    //consulta si se está codificando algo.
    public boolean isEncoding() {
        return isEncoding;
    }

    // convierte un  BufferedImage en un byte[]
    private byte @NotNull [] toByteArray(BufferedImage bi) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpeg", baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }

    // convierte un byte[] en un objeto BufferImage
    private BufferedImage toBufferedImage(byte[] bytes) throws IOException {

        InputStream is = new ByteArrayInputStream(bytes);
        BufferedImage bi = ImageIO.read(is);
        return bi;

    }

}
