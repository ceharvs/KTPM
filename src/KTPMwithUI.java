package KTPM;

import sim.engine.*;
import sim.display.*;
import sim.portrayal.continuous.*;
import javax.swing.*;

import java.awt.Color;


public class KTPMwithUI extends GUIState{
	public Display2D display;
	public JFrame displayFrame;
	
	org.jfree.data.xy.XYSeries series1;
	sim.util.media.chart.TimeSeriesChartGenerator chart1; 
	
	org.jfree.data.xy.XYSeries LTseries;
	org.jfree.data.xy.XYSeries DTseries;
	org.jfree.data.xy.XYSeries Deadseries;
	sim.util.media.chart.TimeSeriesChartGenerator chart2; 
	
	org.jfree.data.xy.XYSeries LTpercent;
	org.jfree.data.xy.XYSeries DTpercent;
	org.jfree.data.xy.XYSeries DWpercent;
	sim.util.media.chart.TimeSeriesChartGenerator chart3; 
	
	 public static void main(String[] args)
     {
		 //new KTPMwithUI().createController();
		 KTPMwithUI vid = new KTPMwithUI();
		 Console c = new Console(vid);
		 c.setVisible(true);
		 c.setWhenShouldEnd(25);
     }
	 
	 public Object getSimulationInspectedObject() { return state; } 
	 ContinuousPortrayal2D agentsPortrayal = new ContinuousPortrayal2D();
	
	 public KTPMwithUI() { super(new PatientsApplet( System.currentTimeMillis())); }
	 public KTPMwithUI(SimState state) { super(state); }

	 public static String getName() { return "KTPM Model"; }

	 
	 public void start()
     {
		 super.start();
  
		 chart1.removeAllSeries();
		 series1 = new org.jfree.data.xy.XYSeries("Wait List Size",false);
		 chart1.addSeries(series1, null);
		 PatientsApplet patients = (PatientsApplet) state;
		 series1.add(0,patients.numPatients,true);
		 
		 scheduleRepeatingImmediatelyAfter(new Steppable()
         {   public void step(SimState state){
        	 PatientsApplet patients = (PatientsApplet) state;
         	 int WLsize = patients.currentWait;
             double t = state.schedule.getTime();               
             series1.add(t+1, WLsize, true); }});
		 
		 chart2.removeAllSeries();
		 LTseries = new org.jfree.data.xy.XYSeries("Living Transplant Recipients",false);
		 chart2.addSeries(LTseries, null).setStrokeColor(Color.green);
		 DTseries = new org.jfree.data.xy.XYSeries("Deceased Transplant Recipients",false);
		 chart2.addSeries(DTseries, null).setStrokeColor(Color.blue);
		 Deadseries = new org.jfree.data.xy.XYSeries("Deceased Patients",false);
		 chart2.addSeries(Deadseries, null).setStrokeColor(Color.black);
		 
		 scheduleRepeatingImmediatelyAfter(new Steppable()
         {   public void step(SimState state){
        	 PatientsApplet patients = (PatientsApplet) state;
             double t = state.schedule.getTime();               
             LTseries.add(t,patients.lTrans,true);}});
		
		 scheduleRepeatingImmediatelyAfter(new Steppable()
         {   public void step(SimState state){
        	 PatientsApplet patients = (PatientsApplet) state;
             double t = state.schedule.getTime();               
             DTseries.add(t,patients.dTrans,true); }});
		 scheduleRepeatingImmediatelyAfter(new Steppable()
         {   public void step(SimState state){
        	 PatientsApplet patients = (PatientsApplet) state;
             double t = state.schedule.getTime();               
             Deadseries.add(t,patients.dead,true);}});
		 
		 chart3.removeAllSeries();
		 LTpercent = new org.jfree.data.xy.XYSeries("% recieved Living Trans.", false);
		 chart3.addSeries(LTpercent,null).setStrokeColor(Color.green);
		 DTpercent = new org.jfree.data.xy.XYSeries("% recieved Deceased Trans.",false);
		 chart3.addSeries(DTpercent, null).setStrokeColor(Color.blue);
		 DWpercent = new org.jfree.data.xy.XYSeries("Died Waiting",false);
		 chart3.addSeries(DWpercent, null).setStrokeColor(Color.black);
		 
		 scheduleRepeatingImmediatelyAfter(new Steppable()
         {   public void step(SimState state){
        	 PatientsApplet patients = (PatientsApplet) state;
             double t = state.schedule.getTime();      
             double percent = (double) patients.lTrans / (double) patients.prevWait;
             LTpercent.add(t,percent,true);}});
		
		 scheduleRepeatingImmediatelyAfter(new Steppable()
         {   public void step(SimState state){
        	 PatientsApplet patients = (PatientsApplet) state;
             double t = state.schedule.getTime(); 
             double percent = (double) patients.dTrans / (double) patients.prevWait;
             DTpercent.add(t,percent,true); }});
		 scheduleRepeatingImmediatelyAfter(new Steppable()
         {   public void step(SimState state){
        	 PatientsApplet patients = (PatientsApplet) state;
             double t = state.schedule.getTime();         
             double percent = (double) patients.dieWait / (double) patients.prevWait;
             DWpercent.add(t,percent,true);}});
		 
		 setupPortrayals();
     }
	 
	 public void load(SimState state){
		 super.load(state);
		 setupPortrayals();
     }
	 
	 public void setupPortrayals(){
		 PatientsApplet patients = (PatientsApplet) state;
		agentsPortrayal.setField(patients.env);
	 }
	 
	 public void init(Controller c){
		 super.init(c);
		 chart1 = new sim.util.media.chart.TimeSeriesChartGenerator();
	     chart1.setTitle("Wait List Size vs. time");
	     chart1.setYAxisLabel("Wait List Size (people)");
	     chart1.setXAxisLabel("Time (years)");
	     JFrame frame = chart1.createFrame(this);
	       
	     frame.setVisible(true);
	     frame.pack();
	     c.registerFrame(frame);
	     
	     chart2 = new sim.util.media.chart.TimeSeriesChartGenerator();
	     chart2.setTitle("Patient Outcomes vs. time");
	     chart2.setYAxisLabel("People");
	     chart2.setXAxisLabel("Time (years)");
	     JFrame frame2 = chart2.createFrame(this);
	     
	     frame2.pack();
	     c.registerFrame(frame2);
	 
	     chart3 = new sim.util.media.chart.TimeSeriesChartGenerator();
	     chart3.setTitle("Percentage Outcomes of Wait List");
	     chart3.setYAxisLabel("Percentage of Wait List");
	     chart3.setXAxisLabel("Time (years)");
	     JFrame frame3 = chart3.createFrame(this);
	     
	     frame3.pack();
	     c.registerFrame(frame3);
	 }

	 public void quit(){
		 super.quit();
		 if (displayFrame!=null) displayFrame.dispose();
	     	displayFrame = null;
	        display = null;
	     }
}