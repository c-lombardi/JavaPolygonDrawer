import javafx.scene.shape.Line;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.RenderableImage;
import java.lang.reflect.Array;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Christopher on 3/14/14.
 */
public class Assign4 extends JFrame {
    private JPanel panel;
    private JMenuBar menu;
    private JMenu file, polygon;
    private JMenuItem about, quit, draw, delete, move;
    private JTextField text;
    private Polygon selectedPolygon, addedPolygon;
    private Point selectedPoint;
    private ArrayList<Point> points;
    private ArrayList<Line2D> lineList;
    private ArrayList<Polygon> polygons;
    private boolean drawMode, deleteMode = false, moveMode = false;
    private int selectedPolygonIndex = 0;
    private int mousex, mousey;
    public Assign4()
    {
        super("Polygon Program");
        super.setLayout(new BorderLayout());
        setSize(700,700);
        // Points and Polygons
        points = new ArrayList<Point>();
        polygons = new ArrayList<Polygon>();
        //panel
        panel = new JPanel(new BorderLayout()){
            @Override
            protected void paintComponent(Graphics g){
                super.paintComponent(g);
                g.setColor(Color.black);
                for (Polygon p : polygons)
                {
                    g.drawPolygon(p);
                }
                if (selectedPolygon != null)
                {
                    g.setColor(Color.red);
                    g.drawPolygon(selectedPolygon);
                }
                if (drawMode)
                {
                    if (points.size() > 0)
                    {
                        for (int i = 0; i < points.size()-1; i++)
                        {
                            g.drawLine((int)points.get(i).getX(),(int)points.get(i).getY(), (int)points.get(i+1).getX(), (int)points.get(i+1).getY());
                        }
                        if (selectedPoint != null)
                            g.drawLine(selectedPoint.x, selectedPoint.y, mousex, mousey);
                    }
                }
            }
        };
        panel.setSize(670,600);
        //JMenu
        menu = new JMenuBar();
        file = new JMenu("File");
        polygon = new JMenu("Polygon");
        about = new JMenuItem("About");
        quit = new JMenuItem("Quit");
        draw = new JMenuItem("Draw");
        delete = new JMenuItem("Delete");
        delete.setEnabled(false);
        move = new JMenuItem("Move");
        move.setEnabled(false);
        text = new JTextField(15);
        text.setEditable(false);
        //adding to menus
        file.add(about);
        file.add(quit);
        polygon.add(draw);
        polygon.add(delete);
        polygon.add(move);
        menu.add(file);
        menu.add(polygon);
        //adding to JFrame
        super.add(menu, BorderLayout.NORTH);
        super.add(text, BorderLayout.SOUTH);
        //adding to frame
        add(panel, BorderLayout.CENTER);
        setVisible(true);
        //mouseEvents
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e){
                if (moveMode)
                {
                    delete.setEnabled(false);
                    move.setEnabled(false);
                    draw.setEnabled(true);
                    selectedPolygon = null;
                    repaint();
                    moveMode = false;
                    text.setText("");
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                selectedPoint = new Point(e.getX(), e.getY());
                System.out.println(e.getX() + " " + e.getY());
                ArrayList<Polygon> selectedPolygons = new ArrayList<Polygon>();
                boolean found = false;
                if (!moveMode && !drawMode)
                {
                    for (Polygon p : polygons)
                    {
                        if(p.contains(selectedPoint))
                        {
                            selectedPolygons.add(p);
                            found = true;
                        }
                    }
                    if (!found)
                    {
                        selectedPolygon = null;
                        repaint();
                    }
                    if (selectedPolygons.size() > 0)
                    {
                        selectedPolygon = selectedPolygons.get(selectedPolygonIndex);
                        repaint();
                        selectedPolygonIndex++;
                        if (selectedPolygonIndex >= selectedPolygons.size())
                        {
                            selectedPolygonIndex = 0;
                        }
                    }
                    if (selectedPolygon != null)
                    {
                        draw.setEnabled(false);
                        move.setEnabled(true);
                        delete.setEnabled(true);
                    }
                    else
                    {
                        draw.setEnabled(true);
                        move.setEnabled(false);
                        delete.setEnabled(false);
                    }
                }
                if (drawMode)
                {
                    if (!linesIntersect(false))
                    {
                        if (e.getClickCount() == 1)
                        {
                            points.add(selectedPoint);
                            repaint();
                        }
                        else if (e.getClickCount() == 2 && !linesIntersect(true))
                        {
                            points.add(selectedPoint);
                            repaint();
                            addedPolygon = new Polygon();
                            for (Point p : points)
                            {
                                addedPolygon.addPoint(p.x, p.y);
                            }
                            polygons.add(addedPolygon);
                            repaint();
                            points.clear();

                            drawMode = false;
                            text.setText("");
                        }
                    }
                    else
                    {
                        text.setText("Lines overlap, error!");
                        selectedPoint = points.get(points.size()-1);
                        repaint();
                    }
                }

            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (drawMode)
                {
                    mousex = e.getX();
                    mousey = e.getY();
                    panel.repaint();
                    text.setText("Drawing... X: " + e.getX() + " Y: " + e.getY());
                }
            }
            @Override
            public void mouseDragged(MouseEvent e){
                if (moveMode)
                {
                    if (isPolygonInBounds(e.getX(), e.getY()))
                    {
                        selectedPolygon.translate(e.getX()-selectedPolygon.xpoints[0], e.getY()-selectedPolygon.ypoints[0]);
                        repaint();
                        text.setText("Moving... X: " + e.getX() + " Y: " + e.getY());
                    }
                }
            }

        });
        //actionEvents
        about.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(null, "This program was created by: Christopher Lombardi on 3/17/2014 \n\n In order to draw, click draw and then single click each point on the screen then double click to complete the polygon. Don't cross lines! \n \n To move a polygon, select the polygon (click inside of it and it will turn red) and then select Move from the drop down menu, then drag it to the desired location and release the mouse. \n\n To delete a polygon, select it (click inside of it and it will turn red), then choose delete from the drop down menu.");
            }});
        quit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        draw.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                drawMode = true;
                delete.setEnabled(false);
                move.setEnabled(false);
                text.setText("Drawing...");
            }
        });
        delete.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                text.setText("Deleting...");
                polygons.remove(selectedPolygon);
                selectedPolygonIndex = 0;
                selectedPolygon = null;
                panel.repaint();
                move.setEnabled(false);
                delete.setEnabled(false);
                draw.setEnabled(true);
                text.setText("Successfully Deleted Polygon!");
            }
        });
        move.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                moveMode = true;
                draw.setEnabled(false);
                delete.setEnabled(false);
                text.setText("Moving...");
            }
        });
    }
    public boolean isPolygonInBounds(int x, int y)
    {
        Polygon localPolygon = new Polygon(selectedPolygon.xpoints, selectedPolygon.ypoints, selectedPolygon.npoints);
        boolean returnVal = true;
        int localX=700, localY=700, localMaxX = 0, localMaxY = 0, transX = x - selectedPolygon.xpoints[0], transY = y-selectedPolygon.ypoints[0];
        localPolygon.translate(transX, transY);
        for (int i : localPolygon.xpoints)
        {
            if (i < localX)
                localX = i;
            if (i > localMaxX)
                localMaxX = i;
        }
        for (int i : localPolygon.ypoints)
        {
            if (i < localY)
                localY = i;
            if (i > localMaxY)
                localMaxY = i;
        }
        if (localY < 0)
        {
            returnVal = false;
        }
        else if(localX < 0)
        {
            returnVal = false;
        }
        else if(localMaxX > panel.getWidth())
        {
            returnVal = false;
        }
        else if(localMaxY > panel.getHeight())
        {
            returnVal = false;
        }
        return returnVal;
    }
    public boolean linesIntersect(boolean end)
    {
        boolean returnVal = false;
        if (!end)
        {
            if (points.size() > 2)
            {
                for (Point p : points)
                {
                    if (points.indexOf(p)+2 < points.size())
                    {
                        returnVal = Line2D.linesIntersect(p.getX(), p.getY(), points.get(points.indexOf(p)+1).getX(), points.get(points.indexOf(p)+1).getY()
                                , points.get(points.size()-1).getX(), points.get(points.size()-1).getY()
                                , selectedPoint.getX(), selectedPoint.getY()
                        );
                        if (returnVal)
                            break;
                    }
                }
            }
        }
        else
        {
            if (points.size() > 2)
            {
                for (int i = 1; i < points.size()-1; i++)
                {
                    if (i+2 < points.size())
                    {
                        returnVal = Line2D.linesIntersect(points.get(i).getX(), points.get(i).getY(),
                                points.get(i+1).getX(), points.get(i+1).getY()
                                , points.get(0).getX(), points.get(0).getY()
                                , selectedPoint.getX(), selectedPoint.getY()
                        );
                        if (returnVal)
                            break;
                    }
                }
            }
        }
        return returnVal;
    }
    public static void main(String [] args) {
        Assign4 application = new Assign4();
        application.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
