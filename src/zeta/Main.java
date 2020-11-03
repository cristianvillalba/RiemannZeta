package zeta;

import com.jme3.app.SimpleApplication;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Line;
import com.jme3.scene.shape.Quad;
import com.jme3.system.JmeSystem;
import com.jme3.texture.Image;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.image.ColorSpace;
import com.jme3.texture.image.ImageRaster;
import com.jme3.util.BufferUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import javax.jws.Oneway;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 * @author normenhansen
 */
public class Main extends SimpleApplication implements ActionListener{
    private Texture texture;
    private ImageRaster imageRaster;
    private Image imagefinal;
    
    public static int width = 1024;
    public static int height = 1024;
    
    //public static int width = 1024;
    //public static int height = 1024;
    
    
    public static float wextension = 20.0f; //graphic plot size in width
    public static float hextension = 50.0f; //grapic plot size in height
    public static float woffset = -2.0f; //graphic plot offset in x
    public static float hoffset = -5.0f; //grapic plot offset in y
    public static int ziteration = 20;
    
    private int pixel = 0;
    private int pixellimit = width * height;
    
    private float[] ek = new float[]{
                        1048575.000000f,1048555.000000f,1048365.000000f,
                        1047225.000000f,1042380.000000f,1026876.000000f,
                        988116.000000f,910596.000000f,784626.000000f,
                        616666.000000f,431910.000000f,263950.000000f,
                        137980.000000f,60460.000000f,21700.001953f,
                        6196.000488f,1351.000122f,211.000000f,21.000000f,1.000000f};
    

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    @Override
    public void simpleInitApp() {
        viewPort.setBackgroundColor(new ColorRGBA(0,0.6f,0.7f,1));
        
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.Blue);
        
        this.initTexture();
        this.paintTexture();
        
        Material matl = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        matl.setTexture("ColorMap",texture);

        //add2DLine(new Vector3f(-100,0,0), new Vector3f(100,0,0), rootNode, 3);
        //add2DLine(new Vector3f(0,0,-100), new Vector3f(0,0,100), rootNode, 3);
        
        Geometry graphRect = new Geometry("GraphRect", new Quad(200, 200));
        graphRect.setMaterial(matl);
        //graphRect.rotate(-FastMath.HALF_PI, 0, 0);
        graphRect.setLocalTranslation(-100, 0,100);
        
        this.flyCam.setMoveSpeed(30f);
        this.initCam();
        
        
        rootNode.attachChild(graphRect);
        
        registerInput();
    }
    
    public void registerInput()
    {
        inputManager.addMapping("save",new KeyTrigger(keyInput.KEY_1));
        inputManager.addMapping("generate",new KeyTrigger(keyInput.KEY_2));
        inputManager.addMapping("predict",new KeyTrigger(keyInput.KEY_3));
        inputManager.addListener(this, "save");
        inputManager.addListener(this, "generate");
        inputManager.addListener(this, "predict");
        
    }

    @Override
    public void simpleUpdate(float tpf) {
        
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //paintPexPixel();
    }
    
    private void initTexture(){
        ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * 4);
        Image image = new Image(Image.Format.RGBA8, width, height, buffer, ColorSpace.sRGB);

        // assign the image to a texture...
        texture = new Texture2D(image);
        imagefinal = image;

        // set some pixel colors...
        imageRaster = ImageRaster.create(image);
    }
    
    public void paintPexPixel()
    {
        if (pixel < pixellimit)
        {
            int i = pixel % width;
            int j = pixel / width;
            
            float x = ((float)i / width)*wextension + woffset;
            float y = ((float)j / height)*hextension + hoffset;

            if (x == 0.5f){ //critical line
                imageRaster.setPixel(i, j, new ColorRGBA(1,1,1,1));
            }
            else if (FastMath.abs(x) < 0.01f) // imaginary axis
            {
                imageRaster.setPixel(i, j, new ColorRGBA(0.0f,0.0f,1.0f,1));
            }
            else if (FastMath.abs(y)< 0.1f) // real axis
            {
                imageRaster.setPixel(i, j, new ColorRGBA(0.0f,0.0f,1.0f,1));
            }
            else if( FastMath.abs(y - 10.0f) < 0.1f) //axis in y = 10
            {
                imageRaster.setPixel(i, j, new ColorRGBA(1.0f,1.0f,0,1));
            }
            else
            {
                imageRaster.setPixel(i, j, ZetaFunction(x, y));
            }
                
            pixel++;
        }
    }
    
    public void paintTexture()
    {
        for (int i = 0; i < width; i++)
        {
            for (int j = 0; j < height; j++)
            {
                
                float x = ((float)i / width)*wextension + woffset;
                float y = ((float)j / height)*hextension + hoffset;
                
                imageRaster.setPixel(i, j, ZetaFunctionFast(x, y));
                //imageRaster.setPixel(i, j, DomainColor(x, y));
                
                
                if (x == 0.5f){ //critical line
                    imageRaster.setPixel(i, j, new ColorRGBA(1,1,1,1));
                }
                else if (FastMath.abs(x) < 0.01f) // imaginary axis
                {
                    imageRaster.setPixel(i, j, new ColorRGBA(0.0f,0.0f,1.0f,1));
                }
                else if (FastMath.abs(y)< 0.1f) // real axis
                {
                    imageRaster.setPixel(i, j, new ColorRGBA(0.0f,0.0f,1.0f,1));
                }
                else if( FastMath.abs(y - 10.0f) < 0.1f) //axis in y = 10
                {
                    imageRaster.setPixel(i, j, new ColorRGBA(1.0f,1.0f,0,1));
                }
                
            }
        }
    }
    
    private ColorRGBA ZetaFunction(float r, float i)
    {
        float real = 0f;
        float imaginary = 0f;
        
        for (int j = 1; j < ziteration; j++)
        {
            float divisorreal = FastMath.pow(j, r) * FastMath.cos(i*FastMath.log(j)); //calculating real part of number to complex power
            float divisorimaginary = FastMath.pow(j, r) * FastMath.sin(i*FastMath.log(j)); //calculating img part of number to complex power
            
            real += divisorreal/(FastMath.pow(divisorreal, 2) + FastMath.pow(divisorimaginary, 2)); //dividing 1 by previous number REAL PART
            imaginary -= divisorimaginary/(FastMath.pow(divisorreal, 2) + FastMath.pow(divisorimaginary, 2)); //dividing 1 by previous number Imaginary PART
        }
        
        Vector2f complexvector = new Vector2f(real, imaginary);
        
        if (FastMath.abs(real)< 0.01f && FastMath.abs(imaginary)< 0.01f)
        {
            return new ColorRGBA(0,0,0,1);
        }
        else if(FastMath.abs(real)< 0.01f )
        {
            return new ColorRGBA(0,0,0,1);
        }
        else if( FastMath.abs(imaginary)< 0.01f)
        {
            return new ColorRGBA(0,0,0,1);
        }
        else
        {
            return new ColorRGBA(0.7f,0.7f,0.7f,1);
        }
    }
    
    private ColorRGBA DomainColor(float r, float ii)
    {
        Complex finalc = new Complex(r, ii);
        
        float hue = (float)finalc.getArgument();
        hue += FastMath.PI;
        hue = (hue + FastMath.PI)/FastMath.TWO_PI;
        float light = (FastMath.log((float)finalc.abs(), 1.5f)) - FastMath.floor(FastMath.log((float)finalc.abs(), 1.5f));
        float s = 0.55f;
        
        //if (FastMath.abs((float)finalc.getReal()) < 0.001f || FastMath.abs((float)finalc.getImaginary()) < 0.001)
        //    return new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f);
        
        float[] color = new float[]{hue,light,s};
        int[] colorfinal = new int[3];
        ColorHSL.hsl2rgb(color,colorfinal);
        
        color[0] = colorfinal[0]/256.0f;
        color[1] = colorfinal[1]/256.0f;
        color[2] = colorfinal[2]/256.0f;
        return new ColorRGBA(color[0],color[1],color[2],1.0f);
    }
    
    private Complex alt(int x)
    {
            float[] tbl = new float[2];
            tbl[0] = 1.0f;
            tbl[1] = -1.0f;
            float value = x % 2.0f;
            int valuei = (int)value;
            return new Complex(tbl[valuei],0.0);
    }
    
    private ColorRGBA ZetaFunctionFast(float r, float ii)
    {
        Complex sum1 = new Complex(0.0);
	Complex sum2 = new Complex(0.0);
	Complex k;
        
	for(int i = 1; i <= ziteration; i++)
	{
		k = new Complex(i, 0.0);
		sum1 = sum1.add(alt(i - 1).divide(k.pow( new Complex(r,ii))));
	}
	for(int i = ziteration + 1; i <= 2 * ziteration; i++)
	{
		k = new Complex(i, 0.0);
		sum2= sum2.add(alt(i - 1).multiply(new Complex(ek[i - ziteration - 1],0.0f)).divide(k.pow(new Complex(r,ii))));
	}
    
        sum2 = sum2.divide( new Complex(FastMath.pow(2, ziteration)));
        Complex finalc = sum1.add(sum2).divide(new Complex(1.0f).subtract(new Complex(2.0f).pow(new Complex(1.0f).subtract(new Complex(r,ii)))));
     
        float hue = (float)finalc.getArgument();
        hue += FastMath.PI;
        hue = (hue + FastMath.PI)/FastMath.TWO_PI;
        float light = (FastMath.log((float)finalc.abs(), 1.5f)) - FastMath.floor(FastMath.log((float)finalc.abs(), 1.5f));
        float s = 0.55f;
        
        if (FastMath.abs((float)finalc.getReal()) < 0.001f || FastMath.abs((float)finalc.getImaginary()) < 0.001)
            return new ColorRGBA(0.0f, 0.0f, 0.0f, 1.0f);
        
        float[] color = new float[]{hue,light,s};
        int[] colorfinal = new int[3];
        ColorHSL.hsl2rgb(color,colorfinal);
        
        color[0] = colorfinal[0]/256.0f;
        color[1] = colorfinal[1]/256.0f;
        color[2] = colorfinal[2]/256.0f;
        return new ColorRGBA(color[0],color[1],color[2],1.0f); 
    }
    
    private float ZetaFunctionFastArgument(float r, float ii)
    {
        Complex sum1 = new Complex(0.0);
	Complex sum2 = new Complex(0.0);
	Complex k;
        
	for(int i = 1; i <= ziteration; i++)
	{
		k = new Complex(i, 0.0);
		sum1 = sum1.add(alt(i - 1).divide(k.pow( new Complex(r,ii))));
	}
	for(int i = ziteration + 1; i <= 2 * ziteration; i++)
	{
		k = new Complex(i, 0.0);
		sum2= sum2.add(alt(i - 1).multiply(new Complex(ek[i - ziteration - 1],0.0f)).divide(k.pow(new Complex(r,ii))));
	}
    
        sum2 = sum2.divide( new Complex(FastMath.pow(2, ziteration)));
        Complex finalc = sum1.add(sum2).divide(new Complex(1.0f).subtract(new Complex(2.0f).pow(new Complex(1.0f).subtract(new Complex(r,ii)))));
     
        if (FastMath.abs((float)finalc.getReal()) < 0.01f && FastMath.abs((float)finalc.getImaginary()) < 0.01f && r == 0.5f){
            System.out.println("zero at: " + r + " , " + ii);
            return 0.0f;
        }
        
        return (float)finalc.abs(); 
    }
    
    
    private void initCam()
    {
        cam.setLocation(new Vector3f(0f,100f,200f));
        cam.lookAt(Vector3f.ZERO.clone(), Vector3f.UNIT_Y.clone());
    }
    
    private void add2DLine( Vector3f A, Vector3f B, Node myNode, int color ) {
        Line line = new Line(A.clone(), 
                             B.clone());
        line.setLineWidth(2);
        Geometry geometry = new Geometry("Bullet", line);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        if ( color == 1 ) {
            mat.setColor("Color", ColorRGBA.Blue);
        };
        if ( color == 2 ) {
            mat.setColor("Color", ColorRGBA.Green);
        };
        if ( color == 3 ) {
            mat.setColor("Color", ColorRGBA.Red);
        };
        if ( color > 3 ) {
            mat.setColor("Color", ColorRGBA.Pink);
        };
        
        geometry.setMaterial( mat );           
        mat.getAdditionalRenderState().setFaceCullMode( RenderState.FaceCullMode.Off );
        geometry.setCullHint(Spatial.CullHint.Never);
        myNode.attachChild(geometry);
    }

    @Override
    public void onAction(String name, boolean isPressed, float tpf) {
        if (name.contains("save") && isPressed)
        {
            File newfile = new File("zeta.png");
            try{
                savePng(newfile, imagefinal);
            }
            catch(Exception e)
            {
                
            }
        }
        
        if (name.contains("generate") && isPressed)
        {
            GenerateCSV();
        }
        
        if (name.contains("predict") && isPressed)
        {
            GenerateCSVIndiv();
        }
    }
    
    public void savePng( File f, Image img ) throws IOException {
        OutputStream out = new FileOutputStream(f);
        try {            
            JmeSystem.writeImageFile(out, "png", img.getData(0), img.getWidth(), img.getHeight());  
        } finally {
            out.close();
        }             
    }
    
    private void GenerateCSV()
    {
        float xmin = 0.5f;
        float xmax = 1.0f;
        float ymin = 1f;
        float ymax = 60f;
        
        int xpoints = 100;
        int ypoints = 160000;
        
        float stripedivisionx = (xmax - xmin) / xpoints; 
        float stripedivisiony = (ymax - ymin) / ypoints;
        
        try (PrintWriter writer = new PrintWriter(new File("zeta.csv"))) {

            for (float j = ymin; j < ymax; j+= stripedivisiony)
            {
                StringBuilder sb = new StringBuilder();
                
                for (float i = xmin + stripedivisionx; i < xmax; i+= stripedivisionx)
                {
                    sb.append(ZetaFunctionFastArgument(i, j) + ",");
                }

                //put final z value of critical line
                sb.append(ZetaFunctionFastArgument(xmin, j) + "\n");
                
                writer.write(sb.toString());
            }
            
            System.out.println("done!");

          } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
          }
        
        
    }
    
    private void GenerateCSVIndiv()
    {
        float xmin = 0.5f;
        float xmax = 1.0f;
        float ymin = 60f;
        float ymax = 100f;
        
        int xpoints = 100;
        int ypoints = 160000;
        
        float stripedivisionx = (xmax - xmin) / xpoints; 
        float stripedivisiony = (ymax - ymin) / ypoints;
        
        try (PrintWriter writer = new PrintWriter(new File("zetapredict.csv"))) {

            for (float j = ymin; j < ymax; j+= stripedivisiony)
            {
                StringBuilder sb = new StringBuilder();
                
                for (float i = xmin + stripedivisionx; i < xmax; i+= stripedivisionx)
                {
                    sb.append(ZetaFunctionFastArgument(i, j) + ",");
                }

                //put final end of line
                sb.append("i value: " + j + "\n");
                
                writer.write(sb.toString());
            }
            
            System.out.println("done!");

          } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
          }
        
        
    }
}
