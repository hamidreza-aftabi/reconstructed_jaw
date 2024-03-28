package artisynth.istar.TMJModel.JawTMJ;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import artisynth.core.gui.ControlPanel;
import artisynth.core.inverse.InverseManager;
import artisynth.core.inverse.TrackingController;
import artisynth.core.inverse.InverseManager.ProbeID;
import artisynth.core.mechmodels.AxialSpring;
import artisynth.core.mechmodels.BodyConnector;
import artisynth.core.mechmodels.ExcitationComponent;
import artisynth.core.mechmodels.ForceEffector;
import artisynth.core.mechmodels.FrameMarker;
import artisynth.core.mechmodels.Muscle;
import artisynth.core.mechmodels.MuscleExciter;
import artisynth.core.mechmodels.PlanarConnector;
import artisynth.core.mechmodels.PointPlaneForce;
import artisynth.core.mechmodels.RigidBody;
import artisynth.core.probes.NumericInputProbe;
import artisynth.core.util.ArtisynthIO;
import artisynth.core.util.ArtisynthPath;
import artisynth.core.workspace.DriverInterface;
import artisynth.core.workspace.RootModel;
import maspack.geometry.PolygonalMesh;
import maspack.matrix.Point3d;
import maspack.properties.PropertyList;
import maspack.render.RenderList;
import maspack.render.RenderObject;
import maspack.render.RenderProps;
import maspack.render.Renderer;
import maspack.render.Renderer.LineStyle;

public class JawFemDemoWithBolusInverse extends RootModel implements ActionListener {
   
  
   
   JawModelFEMInverse myJawModel;
   TrackingController myTrackingController;
   ArrayList<String> MuscleAbbreviation = new ArrayList<String>();
   protected String workingDirname = "data/";
   String probesFilename ;
     
   HashMap<String,String> InsversMuscles = new HashMap<String,String>();

   HashMap<String,String> condyleMusclesLeft = new HashMap<String,String>();
   HashMap<String,String> condyleMusclesRight = new HashMap<String,String>();

   HashMap<String,String> ramusMusclesLeft = new HashMap<String,String>();
   HashMap<String,String> ramusMusclesRight = new HashMap<String,String>();

   HashMap<String,String> bodyMusclesLeft = new HashMap<String,String>();
   HashMap<String,String> bodyMusclesRight = new HashMap<String,String>();

   HashMap<String,String> hemisymphysisMusclesLeft = new HashMap<String,String>();
   HashMap<String,String> hemisymphysisMusclesRight = new HashMap<String,String>();
   
   JFrame frame;
   JPanel panel; 
   JSeparator seperator1;
   JCheckBox cb1,cb2,cb3,cb4,cb5,cb6,cb7,cb8;      
   GridBagConstraints gc;
   JLabel label;
   JButton button;
   
   double t=0.75;  //0.5 prot; 0.75 open; 0.225 brux
   public JawFemDemoWithBolusInverse () {
   }

   public JawFemDemoWithBolusInverse (String name){
      super(null);
   }
   
   @Override
   public void build (String[] args) throws IOException {
      super.build (args);
      setWorkingDir();
      myJawModel=new JawModelFEMInverse("jawmodel");
      addModel (myJawModel);
      getRoot (this).setMaxStepSize (0.001);
      
      //addClosingForce ();
      //addOpening();


      for (double i=0.01; i<=2*t; i=i+0.01 ){
         addWayPoint (i);
      }
      addBreakPoint (t);    
      
      //addControlPanel();
      
      
      loadBoluses();
      
      

      condyleMusclesLeft.put("lip","Left Inferior Lateral Pterygoid");
      condyleMusclesLeft.put("lsp","Left Superior Lateral Pterygoid");
      
      condyleMusclesRight.put("rip","Right Inferior Lateral Pterygoid");
      condyleMusclesRight.put("rsp","Right Superior Lateral Pterygoid");


      ramusMusclesLeft.put("lpt", "Left Posterior Temporal");
      ramusMusclesLeft.put("lmt", "Left Middle Temporal");
      ramusMusclesLeft.put("lat", "Left Anterior Temporal");
      ramusMusclesLeft.put("ldm", "Left Deep Masseter");
      ramusMusclesLeft.put("lsm", "Left Superficial Masseter");
      ramusMusclesLeft.put("lmp", "Left Medial Pterygoid");
      
      ramusMusclesRight.put("rpt", "Right Posterior Temporal");
      ramusMusclesRight.put("rmt", "Right Middle Temporal");
      ramusMusclesRight.put("rat", "Right Anterior Temporal");
      ramusMusclesRight.put("rdm", "Right Deep Masseter");
      ramusMusclesRight.put("rsm", "Right Superficial Masseter");
      ramusMusclesRight.put("rmp", "Right Medial Pterygoid");
      
      
      bodyMusclesLeft.put("lpm","Left Posterior Mylohyoid");
      bodyMusclesLeft.put("lam","Left Mylohyoid");

      bodyMusclesRight.put("ram","Right Mylohyoid");
      bodyMusclesRight.put("rpm","Right Posterior Mylohyoid");


      hemisymphysisMusclesLeft.put("lad", "Left Anterior Digastric");
      hemisymphysisMusclesLeft.put("lgh", "Left Geniohyoid" );        
                      
      hemisymphysisMusclesRight.put("rad", "Right Anterior Digastric" );
      hemisymphysisMusclesRight.put("rgh", "Right Geniohyoid" );      

      
      frame = new JFrame();
      panel = new JPanel();
      gc = new GridBagConstraints();
  
      gc.anchor = GridBagConstraints.EAST;
      gc.fill = GridBagConstraints.NONE;
  
      seperator1 = new JSeparator();
  
      cb1 = new JCheckBox("Left Condyle Defect (Left C)");
      cb2 = new JCheckBox("Right Condyle Defect (Right C)");

      cb3 = new JCheckBox("Left Ramus Defect (Left R)");
      cb4 = new JCheckBox("Right Ramus Defect (Right R)");
  
      cb5 = new JCheckBox("Left Body Defect (Left B)");
      cb6 = new JCheckBox("Right Body Defect (Right B)");
  
      cb7 = new JCheckBox("Left HemiSymphysis Defect (Left SH)");
      cb8 = new JCheckBox("Right HemiSymphysis Defect (Right SH)");
  
      button = new JButton("Initialize/Reset");
  
  
      cb1.addActionListener(this);
      cb2.addActionListener(this);
      cb3.addActionListener(this);
      cb4.addActionListener(this);
      cb5.addActionListener(this);
      cb6.addActionListener(this);
      cb7.addActionListener(this);
      cb8.addActionListener(this);

      button.addActionListener(this);
  
      gc.gridx = 0;
      gc.gridy = 1;
      panel.add(cb1,gc);
  
      gc.gridx = 0;
      gc.gridy = 2;
      panel.add(cb2,gc);
  
      gc.gridx = 0;
      gc.gridy = 3;
      panel.add(cb3,gc);
  
      gc.gridx = 0;
      gc.gridy = 4;
      panel.add(cb4,gc);
  
      gc.gridx = 0;
      gc.gridy = 5;
      panel.add(cb5,gc);
  
      gc.gridx = 0;
      gc.gridy = 6;
      panel.add(cb6,gc);
  
      gc.gridx = 0;
      gc.gridy = 7;
      panel.add(cb7,gc);
  
      gc.gridx = 0;
      gc.gridy = 8;
      panel.add(cb8,gc);
  
      seperator1.setOrientation(SwingConstants.HORIZONTAL);
      gc.gridx = 0;
      gc.gridy = 9;
      panel.add(seperator1,gc);
  
      gc.gridx = 0;
      gc.gridy = 10;
      panel.add(button,gc);
  
     panel.setLayout(new GridLayout(0,1));

     frame.setTitle("Urken's Defect Classification (Forward)");
     frame.setSize(330, 500);
     frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.add(panel);
     frame.setVisible(true);
      
      
     inverseSetup();

     
   }
   

   public void inverseSetup() {
      
         InsversMuscles.put("lip","Left Inferior Lateral Pterygoid");
         InsversMuscles.put("rip","Right Inferior Lateral Pterygoid");
         InsversMuscles.put("lsp","Lateral uperior Lateral Pterygoid");
         InsversMuscles.put("rsp","Right Superior Lateral Pterygoid");
         InsversMuscles.put("lad","Left Anterior Digastric");
         InsversMuscles.put("rad","Right Anterior Digastric");
         InsversMuscles.put("lam","Left Anterior Mylohyoid");
         InsversMuscles.put("ram","Right Anterior Mylohyoid");
         InsversMuscles.put("lpm","Left Posterior Mylohyoid");
         InsversMuscles.put("rpm","Right Posterior Mylohyoid");
         InsversMuscles.put("lgh","Left Geniohyoid");
         InsversMuscles.put("rgh","Right Geniohyoid");
         InsversMuscles.put("lat","Left Anterior Temporal");
         InsversMuscles.put("rat","Right Anterior Temporal");
         InsversMuscles.put("lmt","Left Middle Temporal");
         InsversMuscles.put("rmt","Right Middle Temporal");
         InsversMuscles.put("lpt","Left Posterior Temporal");
         InsversMuscles.put("rpt","Right Posterior Temporal");
         InsversMuscles.put("lsm","Left Superficial Masseter");
         InsversMuscles.put("rsm","Right Superficial Masseter");
         InsversMuscles.put("lmp","Left Medial Pterygoid");
         InsversMuscles.put("rmp","Right Medial Pterygoid");
         InsversMuscles.put("ldm","Left Deep Masseter");
         InsversMuscles.put("rdm","Right Deep Masseter");
      
      
          TrackingController myTrackingController = new TrackingController(myJawModel, "incisor_disp");
           
          for (AxialSpring muscle :  (myJawModel.axialSprings ())) {
              for (String invMuscle: InsversMuscles.keySet()) {
                  if (invMuscle.equals(muscle.getName())){
                          myTrackingController.addExciter((ExcitationComponent)muscle);        
                  }       
                  
              }        
          }

          
          myTrackingController.addMotionTarget(myJawModel.frameMarkers().get("lowerincisor"));
          myTrackingController.addMotionTarget(myJawModel.frameMarkers().get("lbite"));
          myTrackingController.addMotionTarget(myJawModel.frameMarkers().get("rbite"));
          myTrackingController.addMotionTarget(myJawModel.frameMarkers().get("CondyleLeft"));
          myTrackingController.addMotionTarget(myJawModel.frameMarkers().get("CondyleRight"));
         

          myTrackingController.addL2RegularizationTerm(0.02);
          myTrackingController.addDampingTerm (0.000005);
          myTrackingController.setMaxExcitationJump (0.1);
          myTrackingController.setNormalizeH(true);
          myTrackingController.createProbesAndPanel (this);
         
 

          addController(myTrackingController);
              
        }
      
   


   @Override
   public void actionPerformed(ActionEvent event) {
                   
           
           checkBoxJob(condyleMusclesLeft, cb1);
           checkBoxJob(condyleMusclesRight, cb2);
           
           checkBoxJob(ramusMusclesLeft, cb3);
           checkBoxJob(ramusMusclesRight, cb4);

           checkBoxJob(bodyMusclesLeft, cb5);
           checkBoxJob(bodyMusclesRight, cb6);
           
           checkBoxJob(hemisymphysisMusclesLeft, cb7);
           checkBoxJob(hemisymphysisMusclesRight, cb8);
           
           
           if (event.getSource() == button) {
                   
                   
                   disableCorrMuscles(bodyMusclesLeft);
                   disableCorrMuscles(bodyMusclesRight);
                   disableCorrMuscles(condyleMusclesLeft);
                   disableCorrMuscles(condyleMusclesRight);
                   disableCorrMuscles(ramusMusclesLeft);
                   disableCorrMuscles(ramusMusclesRight);
                   disableCorrMuscles(hemisymphysisMusclesLeft);
                   disableCorrMuscles(hemisymphysisMusclesRight);
                   
                   
                   enableCorrMuscles(bodyMusclesLeft);
                   enableCorrMuscles(bodyMusclesRight);
                   enableCorrMuscles(condyleMusclesLeft);
                   enableCorrMuscles(condyleMusclesRight);
                   enableCorrMuscles(ramusMusclesLeft);
                   enableCorrMuscles(ramusMusclesRight);
                   enableCorrMuscles(hemisymphysisMusclesLeft);
                   enableCorrMuscles(hemisymphysisMusclesRight);

                  
                   cb1.setSelected(false);
                   cb2.setSelected(false);
                   cb3.setSelected(false);
                   cb4.setSelected(false);
                   cb5.setSelected(false);
                   cb6.setSelected(false);
                   cb7.setSelected(false);
                   cb8.setSelected(false);
           }
                          
   }
   

   
   public void checkBoxJob(HashMap<String,String> corrMuscles, JCheckBox cb) {
           
           if (cb.isSelected()) {
                   disableCorrMuscles(corrMuscles);
                   //loadProbes("adapted11_l.art");
                   System.out.print("-slected");                   
           } 
   }

   
   
   public void enableCorrMuscles(HashMap<String,String> corrMucle) {
                   
      for (Muscle muscle : myJawModel.myAttachedMuscles){
         muscle.setExcitationColor (Color.RED);
         muscle.setMaxColoredExcitation (1);
         myJawModel.addAxialSpring (muscle);
        
      }
       
   }
   

   
   public void disableCorrMuscles(HashMap<String,String> corrMucle) {
           
           for (String name: corrMucle.keySet()) {
                 AxialSpring as = myJawModel.axialSprings().get (name);
                 myJawModel.removeAxialSpring(as);      
           }
           
   }
   
   
   public boolean bolusesLoaded = false;

   ArrayList<FoodBolusInverse> myFoodBoluses = new ArrayList<FoodBolusInverse>();

   protected double bolusDiameter = 10; // mm

   protected double bolusMaxResistance = 55; // N

   protected double bolusStiffness = bolusMaxResistance / (bolusDiameter);

   protected double bolusDamping = 0.01;

   
   public void loadBoluses() {
      if (bolusesLoaded) return;
      createBoluses();
      for (FoodBolusInverse fb : myFoodBoluses) {
         myJawModel.addForceEffector(fb);
         // System.out.println(fb.getName() + " P = "
         // + fb.getPlane().toString("%8.2f"));
         if (fb.getName().equals("leftbolus")) fb.setActive(true);
         else
            fb.setActive(false);
      }
      
      bolusesLoaded = true;
   }

   public void createBoluses() {
      // TODO: create bolus using occlusal plane angle
      Point3d rightbitePos = myJawModel.frameMarkers().get("rbite")
            .getLocation ();
      Point3d leftbitePos = myJawModel.frameMarkers().get("lbite")
            .getLocation();
      createFoodBolus("rightbolus", rightbitePos, (PointPlaneForce) myJawModel
            .forceEffectors().get("RBITE"));
      createFoodBolus("leftbolus", leftbitePos, (PointPlaneForce) myJawModel
            .forceEffectors().get("LBITE"));
      updateBoluses();
   }
   
   public void updateBoluses() {
      System.out.println("bolus dirs updated");
      if (myFoodBoluses.size() >= 2) {
         updateBolusDirection("RBITE", myFoodBoluses.get(0));
         updateBolusDirection("LBITE", myFoodBoluses.get(1));
      }
   }
   
   public void updateBolusDirection(String constraintName, FoodBolusInverse bolus) {
      PointPlaneForce bite = (PointPlaneForce) myJawModel.forceEffectors()
            .get(constraintName);
      if (bite != null && bolus != null) {
         bolus.setPlane(bite);
         // RigidTransform3d XPB = bite.getXDB();
         // // System.out.println(constraintName + " X =\n" +
         // XPB.toString("%8.2f"));
         // bolus.setPlane( getPlaneFromX (XPB));
         // // System.out.println(bolus.getName() + "plane =\n" +
         // bolus.myPlane.toString("%8.2f"));
      }
   }
   
   
   public void createFoodBolus(String bolusName, Point3d location,
      PointPlaneForce plane) {
      FoodBolusInverse fb = new FoodBolusInverse(bolusName, plane, bolusDiameter,
         bolusMaxResistance, bolusDamping);

   RenderProps bolusPtProps = new RenderProps(myJawModel.getRenderProps());
   bolusPtProps.setPointRadius(0.0);
   bolusPtProps.setPointColor(Color.BLACK);

   RigidBody jaw = myJawModel.rigidBodies().get("jaw");
   FrameMarker bolusContactPt = new FrameMarker();
   myJawModel.addFrameMarker(bolusContactPt, jaw, location);
   bolusContactPt.setName(bolusName + "ContactPoint");
   bolusContactPt.setRenderProps(new RenderProps(bolusPtProps));

   fb.setCollidingPoint(bolusContactPt);
   myFoodBoluses.add(fb);
}


   
   public void addClosingForce() throws IOException{      
      for (BodyConnector p : myJawModel.bodyConnectors ()){     
         if (p.getName ().equals ("BiteICP")==false){
            p.setEnabled (false);
            p.getRenderProps ().setVisible (false);
         }  
   }
      ((PlanarConnector)myJawModel.bodyConnectors ().get ("BiteICP")).setUnilateral (false);
      MuscleExciter mex=myJawModel.getMuscleExciters ().get ("bi_close");     
      NumericInputProbe probe = new NumericInputProbe (mex, "excitation",ArtisynthPath.getSrcRelativePath (JawModelFEMInverse.class, "/data/input_activation.txt"));
      probe.setStartStopTimes (0, 1);
      probe.setName ("Closing Muscle Activation");
      addInputProbe (probe);
   }
   
   public void addOpening() throws IOException{
      for (BodyConnector p : myJawModel.bodyConnectors ()){         
            if (p.getName ().equals ("BiteICP")==false){
               p.setEnabled (false);
               p.getRenderProps ().setVisible (false);
            }        
      }           
      MuscleExciter mex=myJawModel.getMuscleExciters ().get ("bi_open");      
      NumericInputProbe probe = new NumericInputProbe (mex, "excitation",ArtisynthPath.getSrcRelativePath (JawModelFEMInverse.class, "/data/input_activation.txt"));
      probe.setStartStopTimes (0, 0.5);
      probe.setName ("Opening Muscle Activation");
      addInputProbe (probe);
   }
      
   public void setWorkingDir() {
      if (workingDirname == null) return;
      // set default working directory to repository location
      File workingDir = new File (
      ArtisynthPath.getSrcRelativePath(JawFemDemoWithBolusInverse.class, workingDirname));
      ArtisynthPath.setWorkingDir(workingDir);        
   }
  
   public void loadProbes(String probesFilename) {
      String probeFileFullPath = (ArtisynthPath.getSrcRelativePath(JawModelFEMInverse.class,"data/"+probesFilename));

      System.out.println("Loading Probes from File: " + probeFileFullPath);
       
      try {
          scanProbes(ArtisynthIO.newReaderTokenizer(probeFileFullPath));
       } catch (Exception e) {
          System.out.println("Error reading probe file");
          e.printStackTrace();
       }
    }
   
   
  public void addControlPanel(){
  
     ControlPanel panel;
     panel = new ControlPanel("Parameter Tuning","LiveUpdate");
     panel.addLabel ("Ligaments");
     panel.addWidget (myJawModel, "StmSlack");
     panel.addWidget (myJawModel, "SphmSlack");
     panel.addWidget ("tm_R", this, "models/jawmodel/multiPointSprings/tm_R:restLength");
     panel.addWidget ("tm_L", this, "models/jawmodel/multiPointSprings/tm_L:restLength");
     panel.addWidget (new JSeparator());
     panel.addLabel ("Elastic Foundation Contact");
     panel.addWidget (myJawModel, "EFYoung");
     panel.addWidget (myJawModel, "EFThickness");
     panel.addWidget (myJawModel, "EFDamping");
     panel.addWidget (myJawModel, "EFNu");
     panel.addWidget (new JSeparator());
     panel.addLabel ("Capsule Render Properties");
     panel.addWidget ("sapsule_r", this, "models/jawmodel/models/capsule_r:renderProps.visible");
     panel.addWidget ("sapsule_l", this, "models/jawmodel/models/capsule_l:renderProps.visible");
     addControlPanel (panel);
     panel.pack ();
     
    
  }
  
  public void attach(DriverInterface driver) {
    
     loadProbes("inverse_probe2.art");
     
  }
  double[] trace = new double[] {
  2.3768318,-99.827457,-40.301746,
  2.3605186084809904,-99.83060699266903,-40.336528836259035,
  2.345426732453308,-99.81881009671866,-40.39585628647735,
  2.318759677070703,-99.80495517645039,-40.48926566792489,
  2.2833685752188173,-99.78722268731204,-40.61017260398795,
  2.242698480435433,-99.76378128226868,-40.751911690080455,
  2.1962987291477245,-99.7342637484195,-40.90967562013761,
  2.1502996293333654,-99.69919457810965,-41.080667411550245,
  2.1022529517363027,-99.6585181411414,-41.261560075402784,
  2.0531781100200788,-99.61140149915066,-41.44802787861027,
  2.0054011103950335,-99.56000600270502,-41.640256114056484,
  1.960948741507822,-99.50505518039449,-41.83709919748767,
  1.9207858821776265,-99.44725563716442,-42.037397569487744,
  1.883910296905034,-99.38735103438009,-42.24057587619429,
  1.8507657002277682,-99.32557183937959,-42.4456382658272,
  1.8218695337266606,-99.26293787819031,-42.653122763453425,
  1.7969816237829312,-99.19917092785585,-42.861761573721054,
  1.77591000482796,-99.1344206496513,-43.0709799483857,
  1.7584108177777495,-99.06883868156785,-43.28029291962572,
  1.7432853284853649,-99.00259430134389,-43.489355329420235,
  1.73039947419426,-98.93565093831123,-43.69754682771529,
  1.7197887900196505,-98.86780497094483,-43.90390720357999,
  1.7114020615107917,-98.79893431533375,-44.1075773598614,
  1.7045054453889672,-98.7296105625822,-44.30879060529192,
  1.6992735078225025,-98.6600330322591,-44.50734141666561,
  1.6954616226722794,-98.590284210758,-44.70295927007125,
  1.692967658069934,-98.52055293538186,-44.895594962633076,
  1.6915254262846764,-98.45103780720291,-45.085274842792636,
  1.6909108454120836,-98.3818362938973,-45.27190869556636,
  1.690910509336975,-98.31303211442541,-45.4554346537055,
  1.6914024944039614,-98.2447046345612,-45.6357997591537,
  1.692219277382287,-98.17692655336025,-45.81296027987271,
  1.6932856653740753,-98.10978813366692,-45.986919412805136,
  1.6945235327628305,-98.04335360175105,-46.157662146169436,
  1.6956239004473623,-97.97755316935242,-46.32487504949848,
  1.6964293866892994,-97.91234033215272,-46.48839897089268,
  1.6965882261777585,-97.84766110271616,-46.64798458476606,
  1.6963138580017287,-97.7836844823401,-46.80376550943345,
  1.6954781776216414,-97.72054198013835,-46.95586099375127,
  1.6942025026791985,-97.6582650463227,-47.104281806499685,
  1.6926306171238057,-97.59702374457464,-47.24928054865141,
  1.6906370953913816,-97.53701764946246,-47.39120334537048,
  1.688513818937401,-97.47821708929584,-47.530049409179725,
  1.6861920704038482,-97.42051446496382,-47.66569387388188,
  1.683698817308474,-97.36387291934493,-47.798108834553325,
  1.6812506645480263,-97.30823905296839,-47.92725379662585,
  1.6788967794361027,-97.25365481340657,-48.05322427373342,
  1.6765159764712947,-97.2000573853926,-48.175939115338146,
  1.674230878163136,-97.14734979159266,-48.29525508070173,
  1.671851115709424,-97.09569860256389,-48.411551889618444,
  1.6692669334809604,-97.04517410787336,-48.52505908019791,
  1.6663990858280369,-96.99576501008886,-48.63587426585765,
  1.6632806766336063,-96.94745648145722,-48.74408481854757,
  1.6599855610165615,-96.90021557618604,-48.84974861405535,
  1.6565908710983814,-96.85400387756114,-48.95291531564348,
  1.6531636972124204,-96.80878543618903,-49.053639246239634,
  1.649749503370431,-96.76453085018687,-49.151986228807594,
  1.6463732537908808,-96.7212153729619,-49.24802941983327,
  1.6430531164455537,-96.67881185721964,-49.34183181263788,
  1.6398191997336387,-96.63729056767691,-49.4334537585654,
  1.6359959000254154,-96.59698412704883,-49.523616571059584,
  1.6318399530421046,-96.55762670193164,-49.612033034687116,
  1.6274192406155468,-96.51910353526635,-49.69865481330436,
  1.6227199370701015,-96.48136447197135,-49.78351008585935,
  1.6178190604322675,-96.44436273134053,-49.866613217506426,
  1.6128022369216897,-96.40805957190659,-49.94799097041113,
  1.6077567076350583,-96.37242664770628,-50.027687286128675,
  1.6028587755130905,-96.33739118271866,-50.10565146469743,
  1.5981238509124855,-96.30294694164395,-50.181962484064115,
  1.5935969037787436,-96.26908209342679,-50.256691606729056,
  1.589215849074391,-96.23581346228382,-50.329972053518304,
  1.5850350371797512,-96.20309971317768,-50.401832796239326,
  1.5810849239849443,-96.1709070990629,-50.47231015871475,
  1.5756525846695628,-96.13976220075236,-50.54256173552427,
  1.5693956773356628,-96.109287142994,-50.612058469931206,
  1.5637694140665617,-96.07890134317681,-50.67967113644594,
  1.558141773759525,-96.04894864457485,-50.746058135449786,
  1.5534237580842416,-96.01898530331397,-50.81048704176251,
  1.5495921216298272,-95.9891960351051,-50.87331371890144,
  1.5464596277868075,-95.95974280909856,-50.934889033001426,
  1.5439699952868522,-95.93066331960742,-50.99535636896528,
  1.5420296944834475,-95.90196056267992,-51.054850380776884,
  1.5405921616919627,-95.87360986351472,-51.11343712992171,
  1.539485519209046,-95.8456327443371,-51.17127769250136,
  1.5386124214869121,-95.81800088179283,-51.22844210337691,
  1.5378983573038458,-95.79067863238501,-51.28498270366751,
  1.5372934155353417,-95.76362821062446,-51.34094168011338,
  1.5370261869831374,-95.73669390319577,-51.39609827264111,
  1.5369039031339002,-95.70993984915967,-51.450655899287696,
  1.5368765414147427,-95.68335632938798,-51.50468509763651,
  1.537257679275851,-95.6567338127527,-51.5578890060065,
  1.5374716424276729,-95.6304240894228,-51.61094809371269,
  1.5375271159915864,-95.6042930721296,-51.66373630101703,
  1.5377876504416883,-95.57807023984691,-51.71585260054886,
  1.53778419852464,-95.55206990072293,-51.76791158882737,
  1.5445528722710726,-95.52391144700312,-51.83082296480145,
  1.5573195455277848,-95.49327693386417,-51.90198100942443,
  1.57315263691958,-95.46007558788888,-51.9792289336947,
  1.590829241472414,-95.42421959805279,-52.06035113810177,
  1.6097280631149569,-95.38642400492495,-52.144874941670906,
  1.6291785224099944,-95.34719137099762,-52.23210242627248,
  1.6486289759850514,-95.30684977328184,-52.321266243735465,
  1.6677583922720252,-95.26569628862146,-52.4117140010111,
  1.686392992519135,-95.22408034340722,-52.503094007833575,
  1.7043552608533363,-95.18216721489844,-52.59482260749597,
  1.7254123227728395,-95.14201037816025,-52.688879319537925,
  1.747163927479928,-95.10198148376038,-52.782143563195206,
  1.7694008853138299,-95.0622387219436,-52.874570452797656,
  1.792176652670805,-95.02261365717496,-52.965505045687955,
  1.8148065867954282,-94.98330618048522,-53.05497204688497,
  1.837084936090887,-94.94434157417862,-53.142702784159866,
  1.8581644517316955,-94.90579257457793,-53.22858078517393,
  1.8781689088540086,-94.86767006099687,-53.31242748496108,
  1.8967228745667586,-94.83013182864104,-53.39439384269333,
  1.9139182039188993,-94.79311562217342,-53.4743158443463,
  1.92965252180803,-94.75672893580911,-53.552369443758856,
  1.9437898270764213,-94.72104046993618,-53.62872407427859,
  1.9575902556998472,-94.68540405155915,-53.70219157129531,
  1.969927836876022,-94.6506684177514,-53.774436811286435,
  1.981737668634346,-94.61616869222647,-53.84429694306137,
  1.9917237212170744,-94.58274059355182,-53.9134850458821,
  2.001263098868354,-94.54949343220557,-53.980497895537304,
  2.009251524591553,-94.51711335988793,-54.04678606413434,
  2.0170066514820877,-94.48493006589071,-54.111199167790126,
  2.023237536869989,-94.45361926857748,-54.175196320375704,
  2.0294230192518623,-94.4223023329989,-54.23724714569301,
  2.0348325699763254,-94.3914844461794,-54.29838262970752,
  2.0388273892138082,-94.36150630965366,-54.359437051926356,
  2.0426755693942926,-94.33153555680452,-54.4189658643201,
  2.046021470532721,-94.30244326331557,-54.47894675529099,
  2.0495267815106866,-94.27320634214678,-54.537498200963654,
  2.0527658055864384,-94.2443070803134,-54.59556898033321,
  2.0552222128315902,-94.21597051513506,-54.65371423813611,
  2.0580434866122785,-94.18746137706714,-54.71055795693119,
  2.0606450077411793,-94.15919583143801,-54.76693653246123,
  2.0629584438363056,-94.13115559303205,-54.82285968192843,
  2.064536744151905,-94.10355055755517,-54.878833094688375,
  2.0665727105484564,-94.07569955241297,-54.93354293218129,
  2.068443166683612,-94.04806270585479,-54.98788532179595,
  2.0700493288811685,-94.02069527701256,-55.04198675893569,
  2.0709922042096607,-93.99378033655314,-55.09629026837204,
  2.0725273597533396,-93.96659521591451,-55.149378964838576,
  2.0737528345420335,-93.9395503632287,-55.20199583881171,
  2.074839944349592,-93.91276291762738,-55.254361742222194,
  2.0758684345300646,-93.88616595418317,-55.30636457005964,
  2.076914808917103,-93.8597356399658,-55.35796837798422,
  2.0779324282642397,-93.83343897710094,-55.40909569821733,
  2.0789734016166017,-93.80733342925987,-55.45986286895878,
  2.080032237840965,-93.78142043147662,-55.51028804035458,
  2.0811492458655563,-93.75566784769623,-55.56030011627856,
  2.0823128190682523,-93.73008155767016,-55.609909238661494,
  2.083548527120919,-93.70467297799127,-55.65912839111976,
  2.084809497475556,-93.6794595084323,-55.70798259775207,
  2.0861772589154572,-93.65442098890631,-55.756429724502176,
  2.087746100785675,-93.62952705920424,-55.804382084606054,
  2.089528497956268,-93.6047953107411,-55.85186671587729,
  2.09153209137939,-93.58023808843214,-55.89889006851286,
  2.093779406030596,-93.555900315011,-55.945510953617394,
  2.096180246609683,-93.53175177375041,-55.99167868973076,
  2.098624499202191,-93.50777616343379,-56.037309572107425,
  2.1011446627129087,-93.48402517615295,-56.0824707714479,
  2.1037344006589764,-93.46051514074318,-56.12717339180218,
  2.1063998346488195,-93.43726373683435,-56.171436808799996,
  2.108962541544444,-93.41436048541229,-56.215426100703915,
  2.111588364603242,-93.39173226012345,-56.25898934266701,
  2.1143271762382647,-93.36936565909642,-56.30209325390421,
  2.117163327651171,-93.34727567065968,-56.34475859392852,
  2.12007905536721,-93.32548358362425,-56.38702076163097,
  2.1230692120238968,-93.30399811090486,-56.428891989187186,
  2.126140938826494,-93.28281853578267,-56.47036510817734,
  2.1290213533677917,-93.26204770598646,-56.51166513738284,
  2.13185853088835,-93.24159340250073,-56.552642294392186,
  2.1350568852774545,-93.22130486210017,-56.5929435192951,
  2.13850531093316,-93.20132694513737,-56.632819524200855,
  2.1419011319387957,-93.18176514577996,-56.672550319026705,
  2.1456780887821076,-93.16235593485587,-56.71154263448628,
  2.1498334404270376,-93.14319454947886,-56.749940738831846,
  2.1542383584782754,-93.12434781858155,-56.78786563080101,
  2.158823363506529,-93.10583782466799,-56.82536741856114,
  2.1635680938812993,-93.08769746634596,-56.86252879160079,
  2.168474296329823,-93.06989639847225,-56.89930119827147,
  2.1734204209784043,-93.05242197495339,-56.93568684803892,
  2.1783214449230917,-93.035191014462,-56.971541903979,
  2.183115073099166,-93.01822477538386,-57.006900826717,
  2.187858800674871,-93.00157143660277,-57.04185808925241,
  2.1926205390418225,-92.9852293676921,-57.07640804665411,
  2.197386088389179,-92.96923299633366,-57.1106309896495,
  2.2022661439646654,-92.95358844182056,-57.14454682525962,
  2.2072700847210487,-92.93828025665715,-57.178124234656195,
  2.212323528862566,-92.92332088417798,-57.21140343009044,
  2.217889143349778,-92.90884545707323,-57.2447869198529,
  2.224376393027706,-92.89499258670965,-57.27852814766544,
  2.2310696531054073,-92.88146343731623,-57.3120503430644,
  2.237879119875645,-92.86820538431508,-57.34526489803552,
  2.2448476209839274,-92.85513460359489,-57.3780100344595,
  2.251922014052437,-92.84228057185709,-57.41036523581056,
  2.2588552233383,-92.82975162695469,-57.44259440682572,
  2.265963329405328,-92.81735609469465,-57.474298208307175,
  2.273260616286315,-92.8051463075779,-57.50559377319143,
  2.2814138521245826,-92.79350228556916,-57.53716525726823,
  2.2900066040776426,-92.78219393974543,-57.568606238222394,
  2.2986310165835606,-92.77105467278051,-57.59964617299286,
  2.3074417533542304,-92.76012168045423,-57.63045319053995,
  2.3163265020620787,-92.74930554754528,-57.66095694883326,
  2.3252595733032,-92.73854924253709,-57.691016675794344,
  2.3342187660451197,-92.72786243381884,-57.720820259146535,
  2.3428033273048663,-92.71705900796073,-57.74997682008413,
  2.3514153345831317,-92.70638076659606,-57.77894514411635,
  2.3604610262911647,-92.69595116600131,-57.80805837882988,
  2.3698853478090243,-92.68573530563847,-57.8373233119203,
  2.379051383148653,-92.67545083219034,-57.86623321468493,
  2.3884306974639973,-92.66521534132461,-57.89507810870693,
  2.3983280289434306,-92.65517805531692,-57.924187754048376,
  2.4080265778333136,-92.645061277709,-57.95302807386759,
  2.417885464763546,-92.63504851426319,-57.98202530640986,
  2.426891050704232,-92.62533277138934,-58.011790900752295,
  2.435662064398616,-92.61579758001741,-58.04205135737221,
  2.445379769691759,-92.60639156175895,-58.07267147953163,
  2.456063123885382,-92.5971349692222,-58.103686556049844,
  2.4677346912650187,-92.58755985535599,-58.13402995854099,
  2.480300794880235,-92.5779917580915,-58.16420385761891,
  2.494209878646188,-92.56866824927239,-58.1946864251101,
  2.508842646647654,-92.5594854826527,-58.225271097093334,
  2.52383900933918,-92.55038033438736,-58.255807201405915,
  2.5390969636249956,-92.54137268664844,-58.28632693240833,
  2.5542391676644147,-92.53262144633175,-58.31720806218714,
  2.569366849157548,-92.52395871675226,-58.34813668378095,
  2.585007496926474,-92.51522101530614,-58.37871760526459,
  2.6011079246586135,-92.50652041058284,-58.409122677131805,
  2.6173830805291907,-92.49787872768462,-58.43939524617679,
  2.63357971408253,-92.48928742203401,-58.46954411598963,
  2.6490499038474242,-92.48053833273804,-58.499167640860584,
  2.663968932893123,-92.47208065298776,-58.5291158230578,
  2.679169099326015,-92.46406352381912,-58.55966909797325,
  2.6937527529456364,-92.45595770269807,-58.58980503838048,
  2.7079688455039195,-92.44773226755342,-58.619526375992805,
  2.722458263873495,-92.43965860772028,-58.649182153753515,
  2.736755455533095,-92.43162555922466,-58.678523389998475,
  2.7522337264146652,-92.42417530831239,-58.70857705463091,
  2.767761495918782,-92.41670013156504,-58.73830987746639,
  2.783620466665566,-92.40919629078726,-58.767660567266105,
  2.7997880605521406,-92.4019318278567,-58.79711411325945,
  2.8163570622888243,-92.39477136982896,-58.826371609916265,
  2.8330718336144627,-92.3876267363049,-58.85526800724957,
  2.8499345492661394,-92.38058278887954,-58.883954924670526,
  2.8669514489755814,-92.37368379166767,-58.91249779309514,
  2.88417566654704,-92.36698224150561,-58.94097603681921,
  2.901421853226867,-92.36038646730913,-58.96921364574409,
  2.9190696089467085,-92.35404672891715,-58.99746036735444,
  2.93710650437111,-92.34792305140269,-59.025643442664816,
  2.9551831072079695,-92.34187331218828,-59.053497884354684,
  2.97319437586331,-92.33590697971073,-59.08099644857613,
  2.9912279361425096,-92.33011559579319,-59.10828101150324,
  3.0089455032623835,-92.32432157305963,-59.13508536804368,
  3.0271152412599314,-92.31885288060553,-59.1620247167846,
  3.0455355208478245,-92.31364685654222,-59.18885660716319,
  3.064264815632997,-92.3087292314882,-59.21567511353409,
  3.0828724909667358,-92.30424786068886,-59.24278942924392,
  3.1009919134826944,-92.30004057141065,-59.26991542774152,
  3.119100047113709,-92.29630143594218,-59.29736435560366,
  3.1371055998520263,-92.29288190133734,-59.32486928845426,
  3.1550506007909505,-92.2897577845192,-59.3523957775432,
  3.172783453726371,-92.28682506073292,-59.379743125654464,
  3.190131599334604,-92.28401702478382,-59.406736668005244,
  3.2071419874918057,-92.28136369502587,-59.43343785730078,
  3.2240150602434063,-92.27889823663429,-59.45988386317371,
  3.240744256275311,-92.27660457753001,-59.48604197026939,
  3.257401006446755,-92.2744391092819,-59.51181442073546,
  3.2741783522132395,-92.27232191033055,-59.53697995435134,
  3.290837360735682,-92.27033585475573,-59.56168546456884,
  3.307618781008747,-92.26855724188687,-59.58603902022682,
  3.3244212793081944,-92.26694936632774,-59.609968513454064,
  3.3411581636009138,-92.26548827699233,-59.633454774254794,
  3.357976972745072,-92.26424243168434,-59.65664613777046,
  3.374494922145187,-92.26301240917897,-59.67919145547784,
  3.3907775844490855,-92.26189836567578,-59.70124383401745,
  3.406922155358372,-92.2610877338357,-59.723130162937316,
  3.422446514398029,-92.26035360975123,-59.74446738993015,
  3.4374689062662687,-92.25976817381732,-59.76537222653312,
  3.45211496867421,-92.25942588751056,-59.78598378694838,
  3.4664117042542375,-92.25931471396625,-59.80626322022888,
  3.4804202668101354,-92.25943738659251,-59.8262186020892,
  3.494168694546143,-92.25978724769476,-59.84584619348451,
  3.507738548634897,-92.26040455656977,-59.86522460768857,
  3.521127883741946,-92.26126312048906,-59.88432994780955,
  3.534251787883561,-92.26231110860915,-59.90307556308156,
  3.547129925076007,-92.26352398423339,-59.921429246226275,
  3.559666698925072,-92.26490264521729,-59.939454430317895,
  3.5721825728727983,-92.26651398209084,-59.957231459169215,
  3.584562856815573,-92.26834999935755,-59.97475111880904,
  3.5968789692390675,-92.2704083137798,-59.99205602543755,
  3.609190522951615,-92.2725967400902,-60.00896646744167,
  3.6214331835795446,-92.27495957189146,-60.02559960033749,
  3.6333632093248482,-92.27741965798941,-60.0418644192682,
  3.645142687881929,-92.28001639843592,-60.0578685457337,
  3.6568872150338154,-92.28274380963114,-60.07362312409042,
  3.6687565477527144,-92.28573163310348,-60.089395984048544,
  3.6805717999170424,-92.28874014701209,-60.10473923934136,
  3.6922763687141145,-92.29184826565252,-60.11978905496625,
  3.704742305906169,-92.29621800588694,-60.1377163953458,
  3.7165931913042165,-92.30106778401468,-60.156832169549055,
  3.728000655690667,-92.30616055895456,-60.17676352932034,
  3.7391837782422717,-92.31118747720868,-60.19695500718872,
  3.7501931101161916,-92.31606903612688,-60.21713923526413,
  3.76101487924433,-92.32077348467853,-60.23717329719243,
  3.7716504765942185,-92.32504099095935,-60.25652757783885,
  3.7819308202000053,-92.32901682306502,-60.27528411452726,
  3.791776758087981,-92.33292121755022,-60.29373989340017,
  3.80146150560088,-92.33677747415345,-60.31189111917662,
  3.8110942435065063,-92.34054677651048,-60.32960486144452,
  3.8209694203638045,-92.34419024494443,-60.34673580737623,
  3.831276154584627,-92.34776057498783,-60.363221509612785,
  3.841128920478184,-92.35119125727527,-60.37871223108843,
  3.8513675131080696,-92.354755312577,-60.3937207035221,
  3.8618039870277614,-92.35851783607463,-60.408392305821195,
  3.8724370344030783,-92.36246327856978,-60.42270982109173,
  3.883660456000186,-92.36670399298674,-60.43694238721622,
  3.895854813887282,-92.37118532959279,-60.45097111292679,
  3.9087703062304358,-92.3760355215012,-60.465189132398116,
  3.924383450224912,-92.38155375417546,-60.48118136443641,
  3.940694630616656,-92.38705975234099,-60.49742696735483,
  3.9571931128637376,-92.39255071360184,-60.513820964398505,
  3.974072463858736,-92.39842310895152,-60.53098748402826,
  3.990814909652417,-92.40436460835731,-60.5483075116792,
  4.0079628648755925,-92.41039321114738,-60.56599521563851,
  4.025347397425145,-92.4163486855958,-60.58360374329937,
  4.045921675612195,-92.42310898504604,-60.60366894315564,
  4.067428229843363,-92.42959829560674,-60.62357513831059,
  4.09073049103741,-92.43578598426596,-60.64295529710379,
  4.115104897507383,-92.44227178761176,-60.6630156878343,
  4.140207006456005,-92.44873030350948,-60.6828003887161,
  4.165877330462399,-92.45482570920852,-60.70120104029794,
  4.191293536140185,-92.46080773541456,-60.71857862564129,
  4.216341193675602,-92.46679018260201,-60.7350104582808,
  4.24034564087709,-92.47251700260267,-60.749952279254295,
  4.2637429640734705,-92.47826514525363,-60.76384454063404,
  4.286509925029956,-92.48412645806039,-60.776841610180114,
  4.308517552066961,-92.49014769995746,-60.78900746874072,
  4.329542931285799,-92.49671107731032,-60.801287237795115,
  4.349946488593503,-92.5033720141787,-60.81275198337909,
  4.36916663823142,-92.50967988177639,-60.82267344397589,
  4.387640784208411,-92.51587961673259,-60.831481645536215,
  4.405731215177864,-92.52221404224727,-60.839620233662,
  4.42361725688057,-92.52870757462124,-60.84714856700391,
  4.441236232918744,-92.5353293393365,-60.85401729680198,
  4.4593323557907825,-92.54235445297792,-60.860693095532646,
  4.477640349756174,-92.54969991036722,-60.867191502786525,
  4.496134563348944,-92.5571509312035,-60.873093522102025,
  4.514896183578507,-92.56487330709678,-60.878730488585695,
  4.534327106934719,-92.57291344256566,-60.88444398376426,
  4.554502162599622,-92.58086454733595,-60.889202270307266,
  4.575179044408109,-92.58950905774412,-60.89492096124275,
  4.596630304297829,-92.59813915885377,-60.90030410575905,
  4.61917787068996,-92.60652912393999,-60.90467355852032,
  4.641697669797657,-92.61518032015272,-60.909264414179496,
  4.665009248359175,-92.62363446452315,-60.912931958544085,
  4.688700623915826,-92.63261837556749,-60.91736240203478,
  4.713142425867161,-92.64182363284839,-60.92193425456467,
  4.738844060544185,-92.65093618953877,-60.92580944393161,
  4.76450717918283,-92.66037989965997,-60.93012105914966,
  4.790611167697386,-92.66953706764357,-60.93335268492602,
  4.816490654199471,-92.67902085180522,-60.93693554942685,
  4.842959425099131,-92.68834938170082,-60.9396297358957,
  4.8692914543603845,-92.69814475660542,-60.94288807148678,
  4.896350899405554,-92.70792217382458,-60.9455125570166,
  4.92423309897246,-92.71809184748597,-60.94837320293776,
  4.95214641024559,-92.72869801394323,-60.951905773600735,
  4.98083913945703,-92.73930098599882,-60.954869411185456,
  5.009549545199997,-92.74989201166059,-60.95730132845301,
  5.037593225478252,-92.76079807847998,-60.960067520794446,
  5.065832699563559,-92.77151362090831,-60.961972466127385,
  5.093018440870646,-92.78199252042985,-60.96290255574061,
  5.118954908023302,-92.7928017923551,-60.96417146313215,
  5.144584553481777,-92.80341549802552,-60.96447938753209,
  5.168797917131332,-92.81401762198118,-60.964457397895046,
  5.192773685120672,-92.82431073507982,-60.963230025092734,
  5.2167500208305855,-92.83505498250801,-60.96258495386572,
  5.241271425996652,-92.84556562189529,-60.961027400776445,
  5.26649952968019,-92.85632630711885,-60.9595489844271,
  5.291367979469163,-92.86772391270722,-60.95940226479193,
  5.317434987653217,-92.87841413398905,-60.95745645083613,
  5.343971175844149,-92.88913530474353,-60.95515097502398,
  5.369574276033684,-92.90036472185487,-60.95376782737132,
  5.396137585149903,-92.91103713697667,-60.950602931694014,
  5.4228394459761216,-92.92197373606699,-60.94749481140996,
  5.448200136025351,-92.93334950304181,-60.94506274676448,
  5.4741007440474,-92.94420176825149,-60.94089467106276,
  5.499670532220918,-92.95544522132343,-60.93718787296079,
  5.524738135481622,-92.966667249714,-60.93295000790549,
  5.549436431369216,-92.9779648224326,-60.92836734654582,
  5.574528800698227,-92.98959883108284,-60.92411437192749,
  5.599158489808121,-93.00102016798482,-60.91922315907857,
  5.6232922263989815,-93.01228951577839,-60.91370694629678,
  5.647508659780273,-93.02368951448324,-60.908227116531506,
  5.672406471110444,-93.03516788303895,-60.90259627089854,
  5.69715696988194,-93.04674090247343,-60.89684944511883,
  5.7221712337127055,-93.05823536021214,-60.89060805129508,
  5.746790430659527,-93.07021205627106,-60.88512244433363,
  5.772709085047967,-93.08230781819246,-60.87936412355573,
  5.799006997966994,-93.09486410075368,-60.87415744246918,
  5.826483505428614,-93.10803521239262,-60.869798527902866,
  5.854874040993119,-93.12151073966385,-60.86576466692453,
  5.884010283092982,-93.13522271897087,-60.86190729705058,
  5.913505270833921,-93.14898438876813,-60.8578592535957,
  5.943589543515942,-93.1628496271809,-60.854020420545055,
  5.974146396977517,-93.17672444657359,-60.85028013553948,
  6.0067613777533175,-93.19103834278258,-60.84815752536327,
  6.041501954987792,-93.20630870101951,-60.84798075693851,
  6.076196625397241,-93.22074274140536,-60.84635958598916,
  6.111560734360149,-93.235010565083,-60.844549346626465,
  6.146949940538553,-93.24895894930962,-60.842088174276434,
  6.1822581701799475,-93.26265850836468,-60.83896705471241,
  6.219089717475292,-93.27665906058783,-60.83647897973262,
  6.258377750140897,-93.29072748536323,-60.834428116082535,
  6.2990377494568754,-93.30491889833193,-60.832783122669966,
  6.339767027948051,-93.31892789934174,-60.83076039302864,
  6.379814711086174,-93.33258865585046,-60.82792904597151,
  6.418577305305063,-93.34592216763505,-60.82408248492492,
  6.456410696731755,-93.35923186545213,-60.819564269871705,
  6.493301619182555,-93.3726695641098,-60.81422501146577,
  6.529707585853195,-93.38661224574194,-60.80840341254607,
  6.564978336257504,-93.40082199447838,-60.80136175571135,
  6.598974613830473,-93.4155547220916,-60.792519671422994,
  6.634602629270136,-93.43305493039443,-60.783770019254845,
  6.67512091816137,-93.45591525292505,-60.7762644836552,
  6.724754005639149,-93.48760370936247,-60.77054586182117,
  6.792129365463751,-93.53432611174917,-60.770016661793335,
  6.877011977937483,-93.59260842597354,-60.77130728015837,
  6.955408470882321,-93.64370259513483,-60.76252230733496,
  7.015790344769234,-93.67645155625465,-60.73550632662997,
  7.0563052924119045,-93.69479046419275,-60.69598630436051,
  7.082768141469832,-93.70602399715536,-60.653198560623274,
  7.101360574695145,-93.71506946995578,-60.61223095195373,
  7.114623166567032,-93.72387693225426,-60.57439496119221,
  7.124073396848194,-93.73301452995574,-60.53952973412342,
  7.130696569485231,-93.74242127458905,-60.506596870688455,
  7.134022786275774,-93.75297436135162,-60.477054837542184,
  7.137425944876425,-93.7629518467366,-60.4465656983631,
  7.139309860266463,-93.77411993056303,-60.41783286187592,
  7.143366734892123,-93.78538210512777,-60.388200132395504,
  7.1476150483268786,-93.79780938401498,-60.3597834772065,
  7.154216521470919,-93.81001792318156,-60.330008512936196,
  7.160551000161689,-93.82294366777418,-60.30094356284219,
  7.168626618966687,-93.83527291088404,-60.270176849824395,
  7.1759299339407505,-93.84797742267332,-60.23948150978491,
  7.184659867588143,-93.86013767566219,-60.20693203957872,
  7.192811504259104,-93.87303450395163,-60.17491636634714,
  7.2017197888612685,-93.88574098329012,-60.141082495883914,
  7.2115301560914045,-93.89841701077574,-60.10590606538432,
  7.221291957051799,-93.91181493666292,-60.07018272233712,
  7.231044888561933,-93.9255780565434,-60.03325391405991,
  7.242285764571847,-93.93935842065272,-59.99464291010685,
  7.253556220321152,-93.95387336245943,-59.95466433412732,
  7.266062029233501,-93.96932941985149,-59.913801074453474,
  7.282426232578269,-93.98583481190516,-59.87214698317132,
  7.3033173896732215,-94.00344265385502,-59.83004011610996,
  7.325563853709902,-94.02148219654043,-59.785458373521884,
  7.3471189824500875,-94.03923994172996,-59.73831127630437,
  7.36795902023116,-94.05680127777694,-59.688626853332764,
  7.387900329106846,-94.07436192483229,-59.63687915009865,
  7.406312555890011,-94.09209767364351,-59.58326219526593,
  7.423294871707024,-94.11021608829535,-59.52812502706156,
  7.438130574668156,-94.1286123618751,-59.4708378179551,
  7.451158468571777,-94.14762714205774,-59.41200053195354,
  7.46253932713152,-94.16698537762355,-59.350777236321036,
  7.471879432902836,-94.18708605763705,-59.287443411147386,
  7.480251167257145,-94.20834376582246,-59.22253810528578,
  7.487113943083026,-94.23049886590996,-59.15554103873726,
  7.492816305462308,-94.25378462665418,-59.08688501698979,
  7.497213296768108,-94.27807763622152,-59.01643682774166,
  7.500076443806448,-94.30323920077268,-58.94403305826484,
  7.501365145349959,-94.32934421020533,-58.86939724731682,
  7.501629907136222,-94.35699215383676,-58.793217438068524,
  7.500359382519265,-94.38566205966956,-58.71471567961572,
  7.496178581795238,-94.41546638809653,-58.633806217828784,
  7.490670732791562,-94.4461489508154,-58.54963765346832,
  7.484518379958263,-94.47815433185167,-58.46191264992716,
  7.478466970559023,-94.5116705044448,-58.37005870186984,
  7.471318666455399,-94.54699138596965,-58.27446785514774,
  7.46245938312979,-94.58429747176704,-58.175709232836425,
  7.455214571936933,-94.6226133758155,-58.07061681315682,
  7.445279262978227,-94.66226512280281,-57.959576723926915,
  7.430604373212088,-94.70485884941344,-57.84458301651469,
  7.413174793265537,-94.75095694861902,-57.72703108628419,
  7.393460323339301,-94.7995366840284,-57.60585197583029,
  7.37173204635505,-94.84959219088444,-57.47946620929653,
  7.348249588628693,-94.90125188067125,-57.34723379497801,
  7.323111452640911,-94.95605288145904,-57.21226431638579,
  7.297088358706016,-95.01309311388741,-57.07318918731638,
  7.270667319593372,-95.07167023804618,-56.928856833947314,
  7.245194277432306,-95.1317834171719,-56.7792767219328,
  7.221071601657864,-95.19322761902588,-56.62401642185487,
  7.198761764064041,-95.25661929362454,-56.464098262511165,
  7.179522617993246,-95.32091809205582,-56.29753049569841,
  7.161824759121127,-95.38643531022528,-56.12473345523733,
  7.14552371366172,-95.45348975092668,-55.946164057140614,
  7.129013738239086,-95.5220714041747,-55.761773498839354,
  7.110019989750141,-95.59278871561324,-55.57260269388492,
  7.088142282300054,-95.66502179534538,-55.37751573068097,
  7.062341072639788,-95.73962985337025,-55.177884645982374,
  7.033669799040475,-95.8169581741167,-54.9741581691966,
  7.001960343832028,-95.89617738716487,-54.76447741455848,
  6.968692756440247,-95.97763922145673,-54.549535312155456,
  6.9351316870597906,-96.06052666349882,-54.32744721132368,
  6.900620912092125,-96.1458379543582,-54.100426657429026,
  6.864313587416409,-96.23332370708403,-53.86854701944503,
  6.823659227535219,-96.32306740753049,-53.631985901802935,
  6.775992885820457,-96.41399059149089,-53.38999907217179,
  6.721678159387292,-96.50566580530253,-53.142202038760324,
  6.661629699105658,-96.5986850015991,-52.88910651527809,
  6.598345794170651,-96.69276402550203,-52.629751078855996,
  6.534342273803315,-96.78789648660114,-52.362971616351196,
  6.468339242897823,-96.88344978334257,-52.08701260974997,
  6.400317091632349,-96.97890819046445,-51.8005783104868,
  6.3273101774335565,-97.07463259226606,-51.505804535372626,
  6.247563534890374,-97.16577583369812,-51.19928789586304,
  6.150135442117955,-97.24478018068662,-50.87804461887798,
  6.027667800436796,-97.30813498539388,-50.54127722126945,
  5.894759803912273,-97.37475706212854,-50.2017972683483,
  5.758229013979621,-97.44652025664737,-49.862717856875626,
  5.621214020262125,-97.52285219595558,-49.52309601180496,
  5.484921196210028,-97.60217274132492,-49.1801400016327,
  5.350063431048053,-97.68317868558944,-48.83093991945016,
  5.217399057174475,-97.7657211370506,-48.474728690334075,
  5.08837682245482,-97.85099472778808,-48.11239936096449,
  4.964941906556597,-97.93950359827619,-47.74434904182807,
  4.843824998786824,-98.0316390740966,-47.370696244095775,
  4.722712073379209,-98.12510934410443,-46.98739536825986,
  4.6016702705330434,-98.2223725545557,-46.59799694307731,
  4.481835784605112,-98.32305895704224,-46.20188753350399,
  4.362298876780866,-98.42614864873198,-45.79775309491686,
  4.241580324210373,-98.53106175757316,-45.38425809731534,
  4.119061941050479,-98.64037058950606,-44.964942080901224,
  3.995350530379757,-98.75310267941933,-44.53838342839914,
  3.872765625255778,-98.86877001562686,-44.1034773705401,
  3.7507995868377098,-98.98698320808197,-43.65907815552743,
  3.6294007260095915,-99.107608443207,-43.20424832555972,
  3.5086985755815583,-99.23145524850547,-42.7393199857131,
  3.3879486804269323,-99.36018107981113,-42.265823218232825,
  3.2670084410102467,-99.49171351561355,-41.78007505250137,
  3.1473214765958053,-99.62672588451088,-41.281826995316734,
  3.029753026471953,-99.76624277284688,-40.77118638392743,
  2.922426935821054,-99.89875985797511,-40.29377467277725,
  2.857629988368757,-99.9854957530037,-40.296021213740005,
  2.818755490667178,-99.99807372759267,-40.29532487477398,
  2.796866449534501,-99.98105968293858,-40.29850126161753,
  2.7857136163160057,-99.95579923289208,-40.301026698291224,
  2.7748776706331633,-99.93492508583824,-40.302216613808085,
  2.761577846515179,-99.9218636307643,-40.30275981584545,
  2.745372018252202,-99.9152307841197,-40.30323640990635,
  2.727241629804912,-99.9112073105436,-40.30360592333571,
  2.707509455670952,-99.90774903670635,-40.30397638097293,
  2.686541083210408,-99.90407777520494,-40.30439375379176,
  2.6681953271646544,-99.90118942806907,-40.30627884650569,
  2.655166033141352,-99.89883494710267,-40.30769349734716,
  2.646157013640142,-99.8967965972142,-40.30877911830956,
  2.6392659488886347,-99.89486585003097,-40.309619382522726,
  2.6331823772670906,-99.89306374781643,-40.31034795612601,
  2.62749535604969,-99.89102733184421,-40.31104981117227,
  2.621318657524865,-99.88892082237133,-40.311770633910285,
  2.6145894687083215,-99.88689998202413,-40.312512662886085,
  2.6073507347529703,-99.88481266153283,-40.31326444708749,
  2.599714160676502,-99.88270571793396,-40.31400999941373,
  2.592624015513819,-99.88057591055488,-40.314700655652544,
  2.5857871598211606,-99.87843779429576,-40.315366378569905,
  2.579029136031808,-99.87636917821786,-40.316003002027394,
  2.5723294918452235,-99.87436502381675,-40.31661656282894,
  2.565842216065331,-99.87226196205924,-40.31721224491233,
  2.559163680680698,-99.87000899063155,-40.31780153843676,
  2.552114379968225,-99.86759794669203,-40.318391214521434,
  2.544540356153208,-99.86477397026897,-40.31931629724433,
  2.5363856618574347,-99.86123345612671,-40.320466650366285,
  2.5274273385677355,-99.85724286979982,-40.321810937577496,
  2.5174928784179014,-99.85304631009772,-40.32324804912372,
  2.5077556854370844,-99.84837495888522,-40.32464805092248,
  2.4978526200592195,-99.84397528967065,-40.32586311072475,
  2.4882789743619,-99.83988384996664,-40.32681053521581,
  2.4795582070943993,-99.8362483236773,-40.32763230831155,
  2.471777567195072,-99.83298948494685,-40.32832546959433,
  2.4648083391959856,-99.83003811467329,-40.3289155037886,
  2.4591425093455186,-99.82750591571724,-40.3295081550936,
  2.4550585062583186,-99.82548079803647,-40.32995028883669,
  2.4523714800025713,-99.82392254313186,-40.33030558791128,
  2.4505096781021263,-99.82266553324877,-40.33062507222204,
  2.449547428874208,-99.82149974315979,-40.330913535756196,
  2.4484890330157283,-99.82079785498865,-40.33118990351623,
  2.4475133600466417,-99.82021550290159,-40.33144490906347,
  2.447019987942786,-99.8194822027707,-40.331683474328294,
  2.4466822365839014,-99.81887789908478,-40.331920135007074,
  2.446497198043552,-99.81848823517893,-40.3321338353266,
  2.446174811345277,-99.81856109107628,-40.33232339767907,
  2.446223459065081,-99.81880434163314,-40.3324884215176,
  2.4471604454402276,-99.81897938379086,-40.33262855405449,
  2.4481902043917025,-99.81962028662856,-40.33276905858483,
  2.4499408000474108,-99.82024751370798,-40.3328761047368,
  2.452031438163545,-99.82110896964426,-40.332963390040945,
  2.4538799000086424,-99.82244841589477,-40.33303228664529,
  2.455861927319582,-99.82392918796486,-40.33306816358154,
  2.458026800126831,-99.82541905862156,-40.33308190249385,
  2.460415366525577,-99.82694814606202,-40.33308206577173,
  2.4628391232239153,-99.82845717469574,-40.33306113437847,
  2.4652041453102616,-99.82995777964706,-40.33302280470727,
  2.4675060356931495,-99.83150778561756,-40.33296824139886,
  2.4702331317582393,-99.83295321280829,-40.33289216316075,
  2.4726123050733353,-99.83480486679287,-40.332808542859496,
  2.4753816223011214,-99.83656465989385,-40.332678846445944,
  2.4783327488946725,-99.83822349605245,-40.33252824497877,
  2.4810807069921257,-99.84004312962347,-40.332374291996636,
  2.4838035899870494,-99.84208478595065,-40.332185395041776,
  2.486288531926506,-99.84414704249127,-40.331963843309296,
  2.4886962722339585,-99.846168367595,-40.331731037914274,
  2.4909828922256025,-99.84817654787386,-40.331485014326965,
  2.4931923734158086,-99.8502039437345,-40.33123110417948,
  2.4953207322602773,-99.85229015710091,-40.330960164941196,
  2.4973886144065527,-99.85446921884497,-40.33066608557125,
  2.49938025472769,-99.85664129254212,-40.33034403928143,
  2.5013058067114873,-99.85885573964293,-40.33000373856884,
  2.503033399602193,-99.86103205287586,-40.32964543964221,
  2.5044507746370255,-99.8632636029079,-40.329272713458124,
  2.505570231643515,-99.86557553049656,-40.32888053802981,
  2.5068272055952834,-99.86773401351095,-40.3284751089338,
  2.50802633965156,-99.8701327183059,-40.32804127313892,
  2.5089098670122656,-99.87266577883727,-40.32757538000346,
  2.5097410456413605,-99.87507867032332,-40.32709918902146,
  2.510302814071198,-99.87749227627694,-40.32661953785931,
  2.5107950407651862,-99.88013517655702,-40.32610565168787,
  2.5110189782514047,-99.88301890648799,-40.3255505816268,
  2.510985975043842,-99.88595058705877,-40.32497487380983,
  2.5103828934275807,-99.88879640720441,-40.32437787160071,
  2.509558630106429,-99.8916089601012,-40.32378075295923,
  2.5085205558434587,-99.8946599134102,-40.3231200302981,
  2.5072319314885143,-99.89801744827341,-40.3224193176381,
  2.5058374455707453,-99.90155892376069,-40.321706441328914,
  2.5042406422555086,-99.90537119134615,-40.320927642184884,
  2.5024507483003684,-99.90928359215812,-40.32012885132999,
  2.500525070520185,-99.91333459008578,-40.31927379420666,
  2.4990580871104684,-99.91706130803293,-40.31843887220585,
  2.4976109431870857,-99.92075106499948,-40.31761089965221,
  2.4964116304969184,-99.92430637369044,-40.31675929365571,
  2.495082438204826,-99.92798847748682,-40.315890953672934,
  2.493634515546678,-99.93176809697832,-40.31498765194105,
  2.4923195275588954,-99.93559604658147,-40.31403069176867,
  2.4911605820346665,-99.93937964378671,-40.31305060432327,
  2.490011477900991,-99.94309497326171,-40.312066501208044,
  2.488904233884115,-99.94682660401392,-40.31106586240545,
  2.487829401016276,-99.95065460830736,-40.3100059360552,
  2.486811515546238,-99.95446228632463,-40.30894468932907,
  2.485823285953178,-99.95835466211366,-40.30785433637314,
  2.4851254106191356,-99.96205030043632,-40.306777056456816,
  2.484600560647526,-99.96551726802576,-40.30572536676471,
  2.4841256232687496,-99.96868174495417,-40.30473362681506,
  2.483668193877505,-99.97155125931047,-40.303778752890764,
  2.483240111980774,-99.97417320446479,-40.30285386437033,
  2.482874250834647,-99.97653826246403,-40.30197057948049,
  2.482539711708231,-99.9786589721761,-40.30111936206625,
  2.4822586617051865,-99.98061824884596,-40.300274490868446,
  2.482058705407231,-99.9823722757932,-40.29946358791984,
  2.4819373726405547,-99.98397772943764,-40.298663106805414,
  2.481897497724363,-99.98539598138896,-40.29789336831598,
  2.481987096022409,-99.98664539574125,-40.29715584517524,
  2.482235128992822,-99.98773868738915,-40.296433074174814,
  2.482677919215398,-99.98866513909623,-40.29574767813659,
  2.48334026473061,-99.98944564573284,-40.29506934450838,
  2.4841588147418148,-99.99007179537423,-40.294391168831616,
  2.4851824298660796,-99.99052817060647,-40.293738848703775,
  2.486411749574873,-99.99084511486816,-40.29309816446713,
  2.4878645313360495,-99.99094731212378,-40.292483619599885,
  2.4893305024199104,-99.99094534345869,-40.2919058969501,
  2.490982173030286,-99.99085702478891,-40.29136508640197,
  2.49288253067407,-99.99072517702375,-40.29083945881633,
  2.4946208870789883,-99.99049462704556,-40.29040476571156,
  2.4957435819165994,-99.99008462483368,-40.29010215994442,
  2.496150717468851,-99.98945958038692,-40.28987498685955,
  2.4959332815026767,-99.98867057229745,-40.28971308692922,
  2.4952497157479088,-99.98777512232304,-40.28960334558261,
  2.4942534632492026,-99.98683208700297,-40.289523564720156,
  2.493118311661512,-99.985865373809,-40.2894517153355,
  2.4921838711287614,-99.9850981937708,-40.289360331128805,
  2.491627984150925,-99.98468384162405,-40.28922674320225,
  2.4914397405582798,-99.98461283264349,-40.28901529503353,
  2.4916952984515284,-99.98488966736326,-40.288744983168876,
  2.492388423298253,-99.98546141625329,-40.28842500861624,
  2.4934256279146254,-99.98627782734704,-40.288059018338714,
  2.494690603550675,-99.9872816248093,-40.28766637948153,
  2.4962130274768186,-99.98832845591042,-40.28724610159059,
  2.497748184540843,-99.98932391590347,-40.28681810275736,
  2.4992477046487043,-99.9902512607701,-40.28639929777829,
  2.5006934432266616,-99.99109537899811,-40.28598499574639,
  2.5020728856550805,-99.9918505518564,-40.28558424595758,
  2.503521239130854,-99.99258132219701,-40.28519502638956,
  2.5052269287989772,-99.9933549536309,-40.28480869184446,
  2.507269704383958,-99.99418710056696,-40.28441076609962,
  2.5096765920547366,-99.99508216778466,-40.28397605219412,
  2.5123860494929877,-99.99601978608743,-40.283503865014396,
  2.5153294881928736,-99.99699288091298,-40.283009957566605,
  2.5184506449999007,-99.99797083529793,-40.282557277217734,
  2.521925408013211,-99.99897835607567,-40.28222881959684,
  2.5259205879841855,-100.00001418951804,-40.28198469232923,
  2.530420187810541,-100.00106403382634,-40.281799174341046,
  2.5353451310034005,-100.00214236113685,-40.281691100638035,
  2.540691896007528,-100.00326047026198,-40.28160871647562,
                             
       };
       
    
    RenderObject traceObj;

       public void prerender (RenderList list) {
          super.prerender (list);
          if (traceObj == null) {
             RenderObject rob = new RenderObject();
             rob.createLineGroup();
             int vidx = 0;
             Point3d p0 = new Point3d (trace[0], trace[1], trace[2]);
             for (int i=1; i<trace.length/3; i++) {
                Point3d p1 = new Point3d (trace[3*i+0], trace[3*i+1],trace[3*i+2]);
               rob.addPosition (p0);
                rob.addVertex (vidx);
                rob.addPosition (p1);
                rob.addVertex (vidx+1);
                rob.addLine (vidx, vidx+1);
                vidx += 2;
                p0 = p1;
             }
             traceObj = rob;
          }
       }
    
       public void render (Renderer r, int flags) {
          super.render (r, flags);
    
          RenderObject rob = traceObj;
          if (rob != null) {
             rob.lineGroup (0);
             double radius = 0.5; // <- set the radius here
             r.setColor (Color.MAGENTA); // <- set the color here
             r.drawLines (rob, LineStyle.CYLINDER, radius);
          }
       }
  
}
