package Stream;

import org.jetbrains.annotations.NotNull;

import org.jcodec.api.awt.AWTSequenceEncoder;
import org.jcodec.common.io.NIOUtils;
import org.jcodec.common.io.SeekableByteChannel;
import org.jcodec.common.model.Rational;
import java.awt.image.BufferedImage;

import javax.imageio.ImageIO;
import java.io.*;

public class VideoManager {

    private SeekableByteChannel out;

    private AWTSequenceEncoder encoder;

    private boolean isEncoding = false;

    public VideoManager() {
    }

    public boolean startMp4Encode(String path) throws IOException {

        //solo se puede iniciar una nueva codificación en curso si no hay una uya existente
        if(!isEncoding){

            out = NIOUtils.writableFileChannel(path);
            encoder = new AWTSequenceEncoder(out, Rational.R(3, 1));
            isEncoding = true;
            return true;

        }

        return false;
        
    }
    
    public void nextFrame(byte[] image) throws IOException {

        encoder.encodeImage(toBufferedImage(image));
        
    }

    public void stopAndSave() throws IOException {
        encoder.finish();
        NIOUtils.closeQuietly(out);
        isEncoding = false;
    }


    // convert BufferedImage to byte[]
    private byte @NotNull [] toByteArray(BufferedImage bi) throws IOException {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bi, "jpeg", baos);
        byte[] bytes = baos.toByteArray();
        return bytes;

    }

    // convert byte[] to BufferedImage
    private BufferedImage toBufferedImage(byte[] bytes) throws IOException {

        InputStream is = new ByteArrayInputStream(bytes);
        BufferedImage bi = ImageIO.read(is);
        return bi;

    }

    private boolean saveBinaryFile(byte[] bytes, String path)  {

        try {

            FileOutputStream ficheroSalida = new FileOutputStream(path);
            ficheroSalida.write(bytes);
            ficheroSalida.close();
            return true;

        }catch (Exception ex){

            return false;
        }
    }

}
