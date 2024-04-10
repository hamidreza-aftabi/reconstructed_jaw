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

import artisynth.core.femmodels.FemElement;
import artisynth.core.femmodels.FemElement3d;
import artisynth.core.femmodels.FemFactory;
import artisynth.core.femmodels.FemModel;
import artisynth.core.femmodels.FemModel3d;
import artisynth.core.femmodels.FemNode3d;
import artisynth.core.femmodels.HexElement;
import artisynth.core.femmodels.FemModel.Ranging;
import artisynth.core.femmodels.FemModel.SurfaceRender;
import artisynth.core.gui.ControlPanel;
import artisynth.core.inverse.TrackingController;
import artisynth.core.materials.LinearMaterial;
import artisynth.core.mechmodels.AxialSpring;
import artisynth.core.mechmodels.BodyConnector;
import artisynth.core.mechmodels.CollisionManager;
import artisynth.core.mechmodels.MechModel;
import artisynth.core.mechmodels.Muscle;
import artisynth.core.mechmodels.MuscleExciter;
import artisynth.core.mechmodels.PlanarConnector;
import artisynth.core.mechmodels.PointAttachable;
import artisynth.core.mechmodels.RigidBody;
import artisynth.core.mechmodels.CollisionManager.ColliderType;
import artisynth.core.mechmodels.MechSystemSolver.PosStabilization;
import artisynth.core.modelbase.ComponentUtils;
import artisynth.core.probes.NumericInputProbe;
import artisynth.core.util.ArtisynthIO;
import artisynth.core.util.ArtisynthPath;
import artisynth.core.workspace.RootModel;
import maspack.geometry.Face;
import maspack.geometry.PolygonalMesh;
import maspack.matrix.Point3d;
import maspack.matrix.RigidTransform3d;
import maspack.matrix.SymmetricMatrix3d;
import maspack.matrix.Vector3d;
import maspack.properties.PropertyList;
import maspack.render.RenderProps;
import maspack.render.Renderer.LineStyle;
import maspack.util.DoubleInterval;
import maspack.util.PathFinder;


public class JawFemDemo extends RootModel implements ActionListener {
  
   
   JawModelFEM myJawModel; 
   TrackingController myTrackingController;
   
   boolean myUseCollisions = true;

   boolean myShowDonorStress = true;
   
   boolean myUseScrews = true;

   
   
   double DENSITY_TO_mmKS = 1e-9; // convert density from MKS tp mmKS
   double PRESSURE_TO_mmKS = 1e-3; // convert pressure from MKS tp mmKS

   double myBoneDensity = 1900.0 * DENSITY_TO_mmKS;
   double myBoneE = 17*1e9 * PRESSURE_TO_mmKS;
   double myTitaniumDensity = 4420.0 * DENSITY_TO_mmKS;
   double myTitaniumE = 100*1e9 * PRESSURE_TO_mmKS;
   double myTitaniumNu = 0.3;

   
   double myBoneNu = 0.3;
   
   FemModel3d myDonor0;
   FemModel3d myPlate;
   RigidBody myMandibleRight;
   RigidBody myMandibleLeft;



   private static Color PALE_BLUE = new Color (0.6f, 0.6f, 1.0f);
   private static Color GOLD = new Color (1f, 0.8f, 0.1f);

   String myGeoDir = PathFinder.getSourceRelativePath (
      JawFemDemo.class, "geometry/");
   
   
   ArrayList<String> MuscleAbbreviation = new ArrayList<String>();
   ArrayList<Integer> RightSurfaceElemnets = new ArrayList<Integer>();
   ArrayList<Integer> LeftSurfaceElemnets = new ArrayList<Integer>();


   protected String workingDirname = "data/";
   String probesFilename ;

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
   
   
   protected static PropertyList myProps = new PropertyList (JawFemDemo.class, RootModel.class);
  
   
   public PropertyList getAllPropertyInfo() {
      return myProps;
   }

   
   static {
      myProps.addReadOnly ("maxMechanicalStim", "Max Mechanical Stimulus");

    }
   
   public double getMaxMechanicalStim() {
      
      return computeStressStrainDonor0();
     
   }
   
   
   
   double t=0.75; 
   
   public JawFemDemo () {
      
   }

   
   public JawFemDemo (String name){
      super(null);
   }
   
   
   @Override
   public void build (String[] args) throws IOException {
      super.build (args);
      setWorkingDir();
      myJawModel = new JawModelFEM("jawmodel");
      addModel (myJawModel);
      getRoot (this).setMaxStepSize (0.001);
      
      //addClosingForce ();
      addOpening();
      
      
      addFemDonorPlate();
            
      computeStressStrainDonor0();
  
      myJawModel.setStabilization (
         PosStabilization.GlobalStiffness); // more accurate stabilization
      
      ControlPanel panel1 = new ControlPanel("options");

      if (myShowDonorStress) {
         // set donor FEM models to display stress on their surfaces
         myDonor0.setSurfaceRendering (SurfaceRender.Stress);
         myDonor0.setStressPlotRanging (Ranging.Fixed);
         myDonor0.setStressPlotRange (new DoubleInterval(0, 1000));
        
         // allow stress ranges to be controlled in the control panel
         panel1.addWidget ("stressRanging0", myDonor0, "stressPlotRanging");
         panel1.addWidget ("stressRange0", myDonor0, "stressPlotRange");
         
      }
      
      for (double i=0.01; i<=2*t; i=i+0.01 ){
         addWayPoint (i);
      }
      addBreakPoint (t);    
      
      loadProbes("probe.art");
     
      //addControlPanel();
      
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


      hemisymphysisMusclesLeft.put("lad", "Left Anterior Digastric" );
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
     //frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
     frame.add(panel);
     frame.setVisible(true);

   }
   
   
   public double computeStressStrainDonor0(){
      
      myDonor0.setComputeNodalStress (true);
      myDonor0.setComputeNodalStrain (true);
     
      Point3d cent = new Point3d();
      double totalStrainEnergyDensity = 0; // Initialize total strain energy density
      double MaxStrainEnergyDensity =0;
      
      for (int i=0; i<myDonor0.numElements(); i++) {      
        
         FemElement3d elem = myDonor0.getElementByNumber(i);
         elem.computeCentroid (cent);
         PolygonalMesh surfaceRight = myMandibleRight.getSurfaceMesh();

         if (surfaceRight.distanceToPoint (cent) < 2) {
            RightSurfaceElemnets.add (i);
            RenderProps.setLineColor (elem, Color.MAGENTA);
            FemNode3d[] nodes = elem.getNodes ();
            
            double elementStrainEnergyDensity = 0; // Element-specific strain energy density

            for (FemNode3d node : nodes) {
               SymmetricMatrix3d stressTensor = node.getStress();
               SymmetricMatrix3d strainTensor = node.getStrain();

               // Compute the double dot product of stress and strain tensors
               double dotProduct = 0;
               for (int row = 0; row < 3; row++) {
                   for (int col = 0; col < 3; col++) {
                       dotProduct += stressTensor.get(row, col) * strainTensor.get(row, col);
                   }
               }

               // Assuming uniform stress/strain across the element, so average over nodes
               elementStrainEnergyDensity += dotProduct / 2;
           }

            // Average the strain energy density across all nodes for this element
            elementStrainEnergyDensity /= nodes.length;
            

            // Add this element's strain energy to the total
            totalStrainEnergyDensity += elementStrainEnergyDensity;
            
            if (elementStrainEnergyDensity > MaxStrainEnergyDensity) {
               MaxStrainEnergyDensity = elementStrainEnergyDensity;
            }
            
            
         }
         
         
      }

      
      
      return  MaxStrainEnergyDensity/(1000*0.0002) ; // Initialize total strain energy density
 
      
   }
   
 
   
   /**
    * Create a FEM model from a triangular surface mesh using Tetgen.
    *
    * @param mech MechModel to add the FEM model to
    * @param name name of the FEM model
    * @param meshName name of the mesh file in the geometry folder
    * @param density of the FEM
    * @param E Young's modulus for the FEM material
    * @param nu Possion's ratio for the FEM material
    */
   public FemModel3d createFemModel (
      MechModel mech, String name, String meshName,
      double density, double E, double nu) {

      // create the fem and set its material properties
      FemModel3d fem = new FemModel3d (name);
      fem.setDensity (density);
      fem.setMaterial (new LinearMaterial (E, nu));

      // load the triangular surface mesh and then call createFromMesh,
      // which uses tetgen to create a tetrahedral volumetric mesh:
      PolygonalMesh surface = loadMesh (meshName);
      FemFactory.createFromMesh (fem, surface, /*tetgen quality=*/1.5);

      // damping parameters are important for stabilty
      fem.setMassDamping (1.0);
      fem.setStiffnessDamping (0);

      // enable computation of nodal stresses. Do this so that stresses will be
      // computed even if they are not being rendered.
      fem.setComputeNodalStress (true);

      // turn on surface rendering and set surface color to light blue
      RenderProps.setFaceColor (fem, PALE_BLUE);
      fem.setSurfaceRendering (FemModel.SurfaceRender.Shaded);
      RenderProps.setSphericalPoints (fem, 0.25, Color.BLUE);

      mech.addModel (fem);
      return fem;
   }

   

   
   /**
    * Load a polygonal mesh with the given name from the geometry folder.
    */
   private PolygonalMesh loadMesh (String meshName) {
      PolygonalMesh mesh = null;
      String meshPath = myGeoDir + meshName;
      try {
         mesh = new PolygonalMesh (meshPath);
      }
      catch (IOException e) {
         System.out.println ("Can't open or load "+meshPath);
      }
      mesh.transform (myJawModel.amiraTranformation);
      return mesh;
   }

   
   /**
    * Attach an FEM model to another body (either an FEM or a rigid body)
    * by attaching a subset of its nodes to that body.
    *
    * @param mech MechModel containing all the components
    * @param fem FEM model to be connected
    * @param body body to attach the FEM to. Can be a rigid body
    * or another FEM.
    * @param nodeNums numbers of the FEM nodes which should be attached
    * to the body
    */
   public void attachFemToBody (
      MechModel mech, FemModel3d fem, PointAttachable body, int[] nodeNums) {

      for (int num : nodeNums) {
         mech.attachPoint (fem.getNodeByNumber(num), body);
      }
   }

   
   /**
    * Attach an FEM model to another body (either an FEM or a rigid body) by
    * attaching all surface nodes that are within a certain distance of the
    * body's surface mesh.
    *
    * @param mech MechModel containing all the components
    * @param fem FEM model to be connected
    * @param body body to attach the FEM to. Can be a rigid body
    * or another FEM.
    * @param dist distance to the body surface for attaching nodes
    */
   public void attachFemToBody (
      MechModel mech, FemModel3d fem, PointAttachable body, double dist) {
      
      PolygonalMesh surface = null;
      if (body instanceof RigidBody) {
         surface = ((RigidBody)body).getSurfaceMesh();
      }
      else if (body instanceof FemModel3d) {
         surface = ((FemModel3d)body).getSurfaceMesh();
      }
      else {
         throw new IllegalArgumentException (
            "body is neither a rigid body nor an FEM model");
      }
      for (FemNode3d n : fem.getNodes()) {
         if (fem.isSurfaceNode (n)) {
            double d = surface.distanceToPoint (n.getPosition());
            if (d < dist) {
               mech.attachPoint (n, body);
               // set the attached points to render as red spheres
               RenderProps.setSphericalPoints (n, 0.5, Color.RED);
            }
         }
      }
   }

   
   
   
   public void addFemDonorPlate() {
     
      //donor
      myDonor0 = createFemModel (
         myJawModel, "donor0", "resected_donor_transformed_remeshed.obj", myBoneDensity, myBoneE, myBoneNu);
      
      
      //plate
      String platePath = myGeoDir + "plate_final.art";
      try {
         // read the FEM using the loadComponent utility
         myPlate = ComponentUtils.loadComponent (
            platePath, null, FemModel3d.class);
         // set the material properties to correspond to titanium 
         myPlate.setName ("plate");
         myPlate.setDensity (myTitaniumDensity);
         myPlate.setMaterial (new LinearMaterial (myTitaniumE, myTitaniumNu));
         myPlate.setMassDamping (10.0);
         myPlate.setStiffnessDamping (0.0);
         
         // set render properties for the plate
         RenderProps.setFaceColor (myPlate, GOLD);
         RenderProps.setPointRadius (myPlate, 0.5);         
      }
      catch (IOException e) {
         System.out.println ("Can't open or load "+platePath);
         e.printStackTrace(); 
      }
      myJawModel.addModel (myPlate);
      
      //attach the plate to the left and right mandible segments. We use
      // explicitly defined nodes to do this, since the plate may be some
      // distance from the segments.
           
      myMandibleRight = (RigidBody)myJawModel.findComponent (
      "rigidBodies/jaw_resected");
      
      myMandibleLeft = (RigidBody)myJawModel.findComponent (
      "rigidBodies/jaw");

      
      int[] leftAttachNodes = {78,57,56,77,76,55};
      
      attachFemToBody (myJawModel, myPlate, myMandibleLeft, leftAttachNodes);
      
      int[] rightAttachNodes = {69,48,70,49,71,50};
     
      attachFemToBody (myJawModel, myPlate, myMandibleRight, rightAttachNodes);

      attachPlateToDonorSegments (myJawModel);

      // set up donor segment interactions
      setDonorSegmentInteractions (myJawModel);
 
   }
   
   
   /**
    * Helper method to attach the plate to the donor segments.
    */
   private void attachPlateToDonorSegments (MechModel mech) {
      if (myUseScrews) {
         // attach plate to donor segments using rigid bodies representing
         // screws
         double screwLen = 10.0;
         double attachTol = 2.0;
         int[] seg0Elems = new int[] {9, 11};
         for (int num : seg0Elems) {
            attachElemToSegment (
               mech, (HexElement)myPlate.getElementByNumber(num),
               myDonor0, screwLen, attachTol);
         }

      }
      
   }

   
   
   
   /**
    * Helper method to set the interactions between donor segments and the
    * mandible segments.
    */
   private void setDonorSegmentInteractions (MechModel mech) {
      if (myUseCollisions) {
         // set the interactions using collisions
         mech.setCollisionBehavior (myDonor0, myMandibleRight, true);
         mech.setCollisionBehavior (myDonor0, myMandibleLeft, true);

         CollisionManager cm = mech.getCollisionManager();
         // use AJL collisions so we can render pressure maps:
         cm.setColliderType (ColliderType.AJL_CONTOUR);
         // set collision manager render properties in case we want to render
         // contact info at some point
         RenderProps.setLineColor (cm, Color.GREEN);
         RenderProps.setLineRadius (cm, 0.5);
         RenderProps.setLineStyle (cm, LineStyle.SOLID_ARROW);
         RenderProps.setVisible (cm, true);        
      }
     
   }

   /**
    * Attach a hex element of plate FEM to one of the donor segment FEMs using
    * a rigid body representation of a screw. The hex element and nearby nodes
    * of the donor FEM at then all connected to the screw.
    *
    * @param mech MechModel containing all the components
    * @param hex hex element of the plate FEM
    * @param donorFem FEM model of the donor segment
    * @param screwLen length of the cylinder representing the screw
    * @param attachTol distance tolerance for attaching donor FEM
    * nodes to the screw
    */
   private void attachElemToSegment (
      MechModel mech, HexElement hex, FemModel3d donorFem,
      double screwLen, double attachTol) {

      // compute centroid of the hex element
      Point3d cent = new Point3d();
      hex.computeCentroid (cent);

      // compute normal pointing toward the donor FEM. From the construction of
      // plate FEM, we know that this is given by the outward facing normal of
      // the quad face given by the first four hex nodes.
      Vector3d nrm = new Vector3d();
      FemNode3d[] nodes = hex.getNodes();
      Face.computeNormal (
         nrm, nodes[0].getPosition(), nodes[1].getPosition(),
         nodes[2].getPosition(), nodes[3].getPosition());

      // represent the screw as a cylinder with radius 1/10 of it length.
      RigidBody screw = RigidBody.createCylinder (
         null, screwLen/10, screwLen, myTitaniumDensity, 10);
      // Set the pose of the screw so that it lies along the normal starting at
      // the hex centroid.
      RigidTransform3d TSW = new RigidTransform3d ();
      TSW.p.set (cent);
      TSW.R.setZDirection (nrm);
      TSW.mulXyz (0, 0, screwLen/2);
      screw.setPose (TSW);

      mech.addRigidBody (screw); // add to the MechModel

      // attach to the screw all donor FEM nodes that are within attachTol of
      // its surface
      PolygonalMesh smesh = screw.getSurfaceMesh();
      int nattach = 0;
      for (FemNode3d n : donorFem.getNodes()) {
         if (smesh.distanceToPoint (n.getPosition()) <= attachTol) {
            mech.attachPoint (n, screw);
            nattach++;
         }
      }
      System.out.println ("screw attached attached with" + nattach + " points");
      // also attach the screw to the hex element
      mech.attachFrame (screw, hex);
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

                   //myJawModel.assembleBilateralExcitors();
                   //myJawModel.assembleMuscleGroups();
                   //loadProbes("adapted11_l.art");
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

    

   
   
   
   public void addClosingForce() throws IOException{      
      for (BodyConnector p : myJawModel.bodyConnectors ()){     
         if (p.getName ().equals ("BiteICP")==false){
            p.setEnabled (false);
            p.getRenderProps ().setVisible (false);
         }  
   }
      ((PlanarConnector)myJawModel.bodyConnectors ().get ("BiteICP")).setUnilateral (false);
      MuscleExciter mex=myJawModel.getMuscleExciters ().get ("bi_close");     
      NumericInputProbe probe = new NumericInputProbe (mex, "excitation",ArtisynthPath.getSrcRelativePath (JawModelFEM.class, "/data/input_activation.txt"));
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
      NumericInputProbe probe = new NumericInputProbe (mex, "excitation",ArtisynthPath.getSrcRelativePath (JawModelFEM.class, "/data/input_activation.txt"));
      probe.setStartStopTimes (0, 0.5);
      probe.setName ("Opening Muscle Activation");
      addInputProbe (probe);
   }
      
   public void setWorkingDir() {
      if (workingDirname == null) return;
      // set default working directory to repository location
      File workingDir = new File (
      ArtisynthPath.getSrcRelativePath(JawFemDemo.class, workingDirname));
      ArtisynthPath.setWorkingDir(workingDir);        
   }
  
   public void loadProbes(String probesFilename) {
      String probeFileFullPath = (ArtisynthPath.getSrcRelativePath(JawModelFEM.class,"data/"+probesFilename));

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
 
}
