package com.patolin.multitouch;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import com.almeros.android.multitouch.*;
import java.util.Random;

/**
 * Created by Patricio on 14/01/2015.
 */
public class mainView extends View {

    private float mScaleSpan=1.0f;
    private float mScaleFactor=1.0f;
    private float mRotationDegrees = 0.f;
    private float mFocusX = 0.f;
    private float mFocusY = 0.f;

    private ScaleGestureDetector mScaleDetector;
    private RotateGestureDetector mRotateDetector;
    private MoveGestureDetector mMoveDetector;

    private cuadrado cuad=new cuadrado();

    private int numCuadrados=0;
    private int cuadActual=0;

    // array de colores
    private int[] colores = { Color.BLUE, Color.GREEN, Color.MAGENTA,
             Color.CYAN, Color.GRAY, Color.RED, Color.DKGRAY,
            Color.LTGRAY, Color.YELLOW };


    private float tapPuntoX,tapPuntoY, contactoTemp;


    SparseArray<cuadrado> arrCuadrados = new SparseArray<cuadrado>();

    public mainView(Context context) {
        super(context);

        // inicializamos los eventos
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
        mRotateDetector = new RotateGestureDetector(context, new RotateListener());
        mMoveDetector = new MoveGestureDetector(context, new MoveListener());

    }

    @Override
    protected void onDraw(Canvas canvas) {

        Paint myPaint=new Paint();

        Paint paintBorde =new Paint();
        paintBorde.setColor(Color.BLACK);
        paintBorde.setStrokeWidth(10);
        paintBorde.setStyle(Paint.Style.STROKE);

        super.onDraw(canvas);

        myPaint.setColor(Color.RED);
        myPaint.setStrokeWidth(1);

        //dibujamos el primer cuadrado, para boton
        canvas.drawRect(0, 0, 250 ,250, myPaint);

        /*
        canvas.save();
        canvas.rotate(mRotationDegrees,cuad.x+(cuad.alto/2),cuad.y+(cuad.ancho/2));
        canvas.drawRect(cuad.x, cuad.y, cuad.x+(cuad.alto*mScaleFactor) , cuad.y+(cuad.ancho*mScaleFactor), myPaint);
        canvas.restore();
        */
        // dibujamos los cuadrados del array

        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);
        for (int i=0;i<arrCuadrados.size();i++) {

            myPaint.setColor(colores[i%8]);
            cuadrado cuadDibujo=arrCuadrados.get(i);

                // grabamos el canvas, rotamos, dibujamos y restauramos el canvas
            canvas.save();
            canvas.rotate(cuadDibujo.angulo,cuadDibujo.x+(cuadDibujo.alto*cuadDibujo.escala/2),cuadDibujo.y+(cuadDibujo.ancho*cuadDibujo.escala/2));
            canvas.drawRect(cuadDibujo.x, cuadDibujo.y, cuadDibujo.x+(cuadDibujo.alto*cuadDibujo.escala) , cuadDibujo.y+(cuadDibujo.ancho*cuadDibujo.escala), myPaint);
            if (i==cuadActual) {
                canvas.drawRect(cuadDibujo.x, cuadDibujo.y, cuadDibujo.x+(cuadDibujo.alto*cuadDibujo.escala) , cuadDibujo.y+(cuadDibujo.ancho*cuadDibujo.escala), paintBorde);

            }
            canvas.restore();

        }

        // dibujamos el area de contacto


        canvas.restore();

        paintBorde.setStrokeWidth(1);
        canvas.drawCircle(mFocusX, mFocusY, contactoTemp*mScaleFactor, paintBorde);
        myPaint.setColor(Color.BLACK);
        canvas.drawText("Seleccion actual: " + cuadActual, 10, 270 , myPaint);
        canvas.drawText("Zoom general: " + mScaleFactor, 10, 290 , myPaint);

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // evento scale almeros
        mScaleDetector.onTouchEvent(event);
        mRotateDetector.onTouchEvent(event);
        mMoveDetector.onTouchEvent(event);

        int maskedAction = event.getActionMasked();

        switch (maskedAction) {
            case MotionEvent.ACTION_DOWN:
                // detectamos que objeto seleccionamos
                float tapX=event.getX();
                float tapY=event.getY();

                tapPuntoX=tapX;
                tapPuntoY=tapY;

                mFocusX=tapX;
                mFocusY=tapY;

                // verificamos si se hace tap sobre el boton de agregar
                if (tapX>0 && tapX<250 && tapY>0 && tapY<250) {
                    cuadrado cuadNuevo = new cuadrado();
                    arrCuadrados.put(numCuadrados, cuadNuevo);
                    numCuadrados++;
                    Log.d("TAG_cuadrado_nuevo", "Cuadrado: " + numCuadrados+"\t"+"x: "+cuadNuevo.x+"\t"+"y: "+cuadNuevo.y);
                }

                // verificamos si se hace tap sobre alguno de los cuadrados
                cuadActual=-1;
                for (int i=0;i<arrCuadrados.size();i++) {
                    cuadrado cuadDetector = arrCuadrados.get(i);
                    float detX= cuadDetector.x*mScaleFactor;
                    float detY= cuadDetector.y*mScaleFactor;
                    float detR= cuadDetector.ancho*cuadDetector.escala*mScaleFactor;

                    if (tapX>(detX) && tapX<(detX+detR) && tapY>(detY) && tapY<(detY+detR)) {
                        cuadActual=i;
                        contactoTemp=detR/2;

                    } else {
                        contactoTemp=0;
                    }

                }
                break;
        }


        this.invalidate();

        return true;
    }



    // clases privadas para usar almeros
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            //mScaleSpan = detector.getCurrentSpan(); // average distance between fingers
            try {
                if (cuadActual<0) {
                    mScaleFactor *= detector.getScaleFactor();
                } else {
                    cuadrado cuadTemp=arrCuadrados.get(cuadActual);
                    cuadTemp.escala*= detector.getScaleFactor();
                    contactoTemp=cuadTemp.ancho*cuadTemp.escala/2;
                    arrCuadrados.setValueAt(cuadActual, cuadTemp);
                }


            } catch(Exception e) {

            }
            return true;

        }
    }

    private class RotateListener extends RotateGestureDetector.SimpleOnRotateGestureListener {
        @Override
        public boolean onRotate(RotateGestureDetector detector) {
            mRotationDegrees -= detector.getRotationDegreesDelta();
            try {
                cuadrado cuadTemp=arrCuadrados.get(cuadActual);
                cuadTemp.angulo-= detector.getRotationDegreesDelta();
                arrCuadrados.setValueAt(cuadActual, cuadTemp);

            } catch(Exception e) {

            }

            return true;
        }
    }

    private class MoveListener extends MoveGestureDetector.SimpleOnMoveGestureListener {
        @Override
        public boolean onMove(MoveGestureDetector detector) {
            PointF d = detector.getFocusDelta();
            mFocusX+=d.x;
            mFocusY+=d.y;
            try {
                // grabamos mFocus en el cuadrado actual
                cuadrado cuadTemp=arrCuadrados.get(cuadActual);
                cuadTemp.x+=d.x/mScaleFactor;
                cuadTemp.y+=d.y/mScaleFactor;

                arrCuadrados.setValueAt(cuadActual, cuadTemp);


            } catch(Exception e) {

            }

            return true;

        }
    }

    // figuras
    private class cuadrado {
        public float x;
        public float y;
        public float ancho;
        public float alto;
        public float angulo;
        public float escala;
        private Random randomGenerator = new Random();

        public cuadrado() {
            this.x=(float)randomGenerator.nextInt(600)+250.0f;
            this.y=(float)randomGenerator.nextInt(600)+250.0f;

            this.ancho=400.0f;
            this.alto=400.0f;

            this.angulo=0.0f;
            this.escala=1.0f;
        }
    }
}
