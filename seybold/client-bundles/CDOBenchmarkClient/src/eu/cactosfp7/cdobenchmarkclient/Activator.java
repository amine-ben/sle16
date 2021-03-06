package eu.cactosfp7.cdobenchmarkclient;

import java.util.Date;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.cdo.transaction.CDOTransaction;
import org.eclipse.emf.cdo.util.CommitException;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import eu.cactosfp7.cdosession.CactosCdoSession;
import eu.cactosfp7.cdosession.settings.CactosUser;
import eu.cactosfp7.cdosessionclient.CdoSessionClient;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.LogicalDCModel;
import eu.cactosfp7.infrastructuremodels.logicaldc.core.VirtualMachine;
import eu.cactosfp7.optimisationplan.ExecutionStatus;
import eu.cactosfp7.optimisationplan.OptimisationPlan;
import eu.cactosfp7.optimisationplan.OptimisationPlanRepository;
import eu.cactosfp7.optimisationplan.OptimisationplanFactory;
import eu.cactosfp7.optimisationplan.SequentialSteps;
import eu.cactosfp7.optimisationplan.StopVmAction;

public class Activator implements BundleActivator  {
	
//	private static final Logger logger = Logger.getLogger(Activator.class.getName());
	private static ScheduledFuture<?> demoRunner;
	private static int maxReads = 100;
	private static Random generator = new Random();
	
	@Override
	public void start(BundleContext context) throws Exception {
		ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
		demoRunner = scheduler.scheduleWithFixedDelay(new Runnable(){

			@Override
			public void run() {
				// change here between create and read plan benchmarks
				//createTrashPlans();
				readTrashPlans(maxReads);
				maxReads += 100; 
			}
        	
        }, 10, 10, TimeUnit.SECONDS);		
	}	
	
	@Override
	public void stop(BundleContext context) throws Exception {
		demoRunner.cancel(false);
	}
	
	private void createTrashPlans(){
		long startTime = System.currentTimeMillis();
		CactosCdoSession cactosCdoSession = CdoSessionClient.INSTANCE.getService()
				.getCactosCdoSession(CactosUser.CACTOSCALE);
		CDOTransaction transaction = cactosCdoSession.createTransaction();
		OptimisationPlanRepository planRepository = (OptimisationPlanRepository) cactosCdoSession.getRepository(transaction,
				cactosCdoSession.getOptimisationPlanPath());
//		CDOID cdoID = planRepository.cdoID();
//		transaction.getObject(cdoID);
		LogicalDCModel logicalRepository = (LogicalDCModel) cactosCdoSession.getRepository(transaction,
				cactosCdoSession.getLogicalModelPath());
		VirtualMachine vm1 = logicalRepository.getHypervisors().get(0).getVirtualMachines().get(0);
		VirtualMachine vm2 = logicalRepository.getHypervisors().get(0).getVirtualMachines().get(1);
		
		int amount=100;
		
		long estimatedTime1 = System.currentTimeMillis() - startTime;
		startTime = System.currentTimeMillis();
		
		// create new plan
		for(int i = 0; i < amount; i++){
			OptimisationPlan plan = OptimisationplanFactory.eINSTANCE.createOptimisationPlan();
			SequentialSteps rootStep = OptimisationplanFactory.eINSTANCE.createSequentialSteps();
			plan.setOptimisationStep(rootStep);
			rootStep.setOptimisationPlan(plan);
			rootStep.setExecutionStatus(ExecutionStatus.READY);
			
			StopVmAction action =  OptimisationplanFactory.eINSTANCE.createStopVmAction();
			action.setStoppedVm(vm1);
			action.setExecutionStatus(ExecutionStatus.READY);
			rootStep.getOptimisationSteps().add(action);
			
			action =  OptimisationplanFactory.eINSTANCE.createStopVmAction();
			action.setStoppedVm(vm2);
			action.setExecutionStatus(ExecutionStatus.READY);
			rootStep.getOptimisationSteps().add(action);			
			
			plan.setExecutionStatus(ExecutionStatus.COMPLETED_SUCCESSFUL);
			plan.setCreationDate(new Date());
			planRepository.getOptimisationPlans().add(plan);
		}
		
		long estimatedTime2 = System.currentTimeMillis() - startTime;
		startTime = System.currentTimeMillis();
		try {
			cactosCdoSession.commitAndCloseConnection(transaction);
		} catch (CommitException e) {
			e.printStackTrace();
		}
		
		long estimatedTime3 = System.currentTimeMillis() - startTime;
		System.out.println("Time for "+amount+" plans: " + estimatedTime1 + "," + estimatedTime2 + "," + estimatedTime3);
	}
	
	private void readTrashPlans(int maxReads){
		long startTime = System.currentTimeMillis();
		CactosCdoSession cactosCdoSession = CdoSessionClient.INSTANCE.getService()
				.getCactosCdoSession(CactosUser.CACTOSCALE);
		CDOTransaction transaction = cactosCdoSession.createTransaction();
		OptimisationPlanRepository planRepository = (OptimisationPlanRepository) cactosCdoSession.getRepository(transaction,
				cactosCdoSession.getOptimisationPlanPath());
		int maxItems = planRepository.getOptimisationPlans().size();
				
		long estimatedTime1 = System.currentTimeMillis() - startTime;
		startTime = System.currentTimeMillis();

		// create new plan
		//int maxReads = 100;
		for(int i = 0; i < maxReads; i++){
			int randomIndex = generator.nextInt(maxItems);
			OptimisationPlan plan = planRepository.getOptimisationPlans().get(randomIndex);
			plan.getExecutionStatus();
			plan.getOptimisationStep();
		}
		
		long estimatedTime2 = System.currentTimeMillis() - startTime;
		startTime = System.currentTimeMillis();
		try {
			cactosCdoSession.commitAndCloseConnection(transaction);
		} catch (CommitException e) {
			e.printStackTrace();
		}		
		long estimatedTime3 = System.currentTimeMillis() - startTime;
		System.out.println("Time for reading "+maxReads+" plans: " + estimatedTime1 + "," + estimatedTime2 + "," + estimatedTime3);		
	}
	
	
}
