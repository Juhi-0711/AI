package src.cornorDoorHuman.hetro;

import java.util.ArrayList;

import src.cornorDoorHuman.hetro.Human;
import sim.util.*;
import sim.field.continuous.*;
import sim.field.network.Network;
import sim.engine.*;


public class Crowd  extends SimState {

	private static final long serialVersionUID = 1;

	public static final int MAXCLUSTER=10;
	public static final int SIZECLUSTER=6; 
	
	// dimensions of the environment
	public static final double XMIN = 0;
	public static final double XMAX = 190;
	public static final double YMIN = 0;
	public static final double YMAX = 190;

	public static Network buddies = new Network(false);

	
	//dimension of the box
	public static final double xMinB = 20;
	public static final double xMaxB = 180;
	public static final double yMinB = 30;
	public static final double yMaxB = 150;

	//where to cut for the door
	public static final double doorLeft = 22;
	public static final double doorRight =30;
	public static final double doorMid = doorLeft + (doorRight-doorLeft)/2;
	public static int upDoorCount =0;
	public static int lwDoorCount =0;

	public int getLwDoorCount()
	{
		return lwDoorCount;
	}

	public int getupDoorCount()
	{
		return upDoorCount;
	}
	
	public double[] getDistribution()
	{
		double[] dis = new double[2];
		dis[0] = (double) (lwDoorCount*100 / (lwDoorCount+upDoorCount));
		dis[1] = (double) (upDoorCount*100 / (lwDoorCount+upDoorCount));
		
		return dis;
	}
	
	// where the obstacles are located (diameter, xpos, ypos)
	static ArrayList< ArrayList<Double>> obstInfo =new ArrayList< ArrayList<Double>>();  //{ {1, 60, 40}, {15, 135, 140} };


	// number of Humans
	public static final int NUM_Humans = 400;

	// the difference between simulation time and Humans time. it is used to compute by how much they moved. can be eliminated, but the speed
	// would need to be increased to maintain the same simulation quality
	public static final double TIMESTEP = 1;

	// for nice displaying, extra space is allocated around the visible area
	public final static double EXTRA_SPACE = -20;

	// the Humans and obstacle environments
	public static Continuous2D HumansEnvironment = null;
	public Continuous2D obstaclesEnvironment = null;
	public static Continuous2D doorEnvironment = null;


	public Crowd(long seed)
	{
		super(seed);
	}


	public void setObjectLocation( final Human hu, Double2D location )
	{
		/*
		// toroidal world!
		double x = (((location.x + EXTRA_SPACE - XMIN) + (XMAX-XMIN  + 2*EXTRA_SPACE)) % (XMAX-XMIN + 2*EXTRA_SPACE)) + XMIN - EXTRA_SPACE;
		double y = (((location.y + EXTRA_SPACE - YMIN) + (YMAX-YMIN  + 2*EXTRA_SPACE)) % (YMAX-YMIN + 2*EXTRA_SPACE)) + YMIN - EXTRA_SPACE;

		location = new Double2D( x, y );
		 */
		HumansEnvironment.setObjectLocation( hu, location );

		// to speed up the simulation, each Human knows where it is located (gets rid of a hash get call)
		hu.x = location.x;
		hu.y = location.y;
	}


	public void start()
	{
		super.start();
		HumansEnvironment = new Continuous2D( Human.MAX_DISTANCE/3, (XMAX-XMIN), (YMAX-YMIN) );
		obstaclesEnvironment = new Continuous2D( /*Math.max(hu.numLinks*DIAMETER,30)*/130, (XMAX-XMIN), (YMAX-YMIN) );
		doorEnvironment = new Continuous2D( Human.MAX_DISTANCE/3, (XMAX-XMIN), (YMAX-YMIN) );
		
		//Test //add only few humans

		int lenX  = (int)(xMaxB - xMinB);
		int lenY  = (int)(yMaxB - yMinB);

		for(int x=0;x<NUM_Humans;x++)
		{
			Double2D loc = null;
			Human hu = null;
			double diameter = 3 + random.nextInt(1) + random.nextFloat();	
			double mass = diameter*10 + random.nextInt(20);
			hu = new Human(diameter,mass);
			loc = new Double2D(xMinB + random.nextInt(lenX-10) + 4.5, yMinB+ random.nextInt(lenY-10) + 4.5);
			HumansEnvironment.setObjectLocation( hu, loc );
			hu.x = loc.x;
			hu.y = loc.y;
			buddies.addNode(hu);
			schedule.scheduleRepeating(hu);
			
		}
		Bag humans = buddies.getAllNodes();
		int n= random.nextInt(MAXCLUSTER);	
		for(int i=0;i<n;i++)
		{
			int c= random.nextInt(SIZECLUSTER);
			Human[] arr= new Human[c];
			for(int j=0;j<c;j++)
			{
				arr[j]=(Human)humans.get(random.nextInt(humans.size()-1));
				arr[j].buds=c;
				
			}
			for(int j=0;j<c;j++)
			{
				for(int k=0;k<c;k++)
				{
					double b=0.9;
					if(k!=j)
					{
						buddies.addEdge(arr[j],arr[k],new Double(b));
					}
				}
			}
			//hu.buds=2;
		}
		// add the obstacles to the simulation
		addObstacle();
		
		//show doorArea
		DoorArea darea1 = new DoorArea();
		doorEnvironment.setObjectLocation(darea1, new Double2D(doorMid,yMinB));
		//lower door
		int diam =  20;
		DoorArea darea2 = new DoorArea();
		doorEnvironment.setObjectLocation(darea2, new Double2D(doorMid,yMaxB));


		}
	public int getCount()
	{
		return HumansEnvironment.getAllObjects().numObjs;
	}
/***********************************
 * Create the box of obstacles
 * 
 ***********************************/
	public void addObstacle() {

	 
		//upper wall vary x y is min fixed
		for(double i = xMinB ; i <xMaxB ; i+=3 )
		{

			ArrayList<Double> arg = new ArrayList<Double>();
			arg.add(2.0);arg.add(i);arg.add(yMinB);



			if(i<doorLeft || i>doorRight)			//door in the room
				obstInfo.add(arg);
		}

		for (int i = 0; i < obstInfo.size(); i++) {

			Obstacle obst = new Obstacle( obstInfo.get(i).get(0) );
			obstaclesEnvironment.setObjectLocation( obst, new Double2D( obstInfo.get(i).get(1), obstInfo.get(i).get(2)) );
		}
		obstInfo.clear();
		
		
		//lower wall vary x y is max fixed
		for(double i = xMinB ; i <xMaxB ; i+=3 )
		{

			ArrayList<Double> arg = new ArrayList<Double>();
			arg.add(2.0);arg.add(i);arg.add(yMaxB);
			
			if(i<doorLeft || i>doorRight)			//door in the room
				obstInfo.add(arg);
		}

		for (int i = 0; i < obstInfo.size(); i++) {

			Obstacle obst = new Obstacle( obstInfo.get(i).get(0) );
			obstaclesEnvironment.setObjectLocation( obst, new Double2D( obstInfo.get(i).get(1), obstInfo.get(i).get(2)) );
		}

		//left wall x is min and y will vary
		obstInfo.clear();
		for(double i = yMinB ; i <yMaxB; i+=3 )
		{

			ArrayList<Double> arg = new ArrayList<Double>();
			arg.add(2.0);arg.add(xMinB);arg.add(i);
			obstInfo.add(arg);
		}
		for (int i = 0; i < obstInfo.size(); i++) {

			Obstacle obst = new Obstacle( obstInfo.get(i).get(0) );
			obstaclesEnvironment.setObjectLocation( obst, new Double2D( obstInfo.get(i).get(1), obstInfo.get(i).get(2)) );
		}
		
		//right wall, x is max and y vary
		for(double i = yMinB ; i <yMaxB; i+=3 )
		{

			ArrayList<Double> arg = new ArrayList<Double>();
			arg.add(2.0);arg.add(xMaxB);arg.add(i);
			obstInfo.add(arg);
		}
		for (int i = 0; i < obstInfo.size(); i++) {

			Obstacle obst = new Obstacle( obstInfo.get(i).get(0) );
			obstaclesEnvironment.setObjectLocation( obst, new Double2D( obstInfo.get(i).get(1), obstInfo.get(i).get(2)) );
		}




	}
	public static void main(String[] args)
	{
		doLoop(Crowd.class, args);
		System.exit(0);
	}    
}


