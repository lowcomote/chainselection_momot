package trafochainselection.demo;

import at.ac.tuwien.big.moea.SearchAnalysis;
import at.ac.tuwien.big.moea.SearchExperiment;
import at.ac.tuwien.big.moea.SearchResultManager;
import at.ac.tuwien.big.moea.experiment.analyzer.SearchAnalyzer;
import at.ac.tuwien.big.moea.experiment.executor.SearchExecutor;
import at.ac.tuwien.big.moea.experiment.executor.listener.SeedRuntimePrintListener;
import at.ac.tuwien.big.moea.print.IPopulationWriter;
import at.ac.tuwien.big.moea.print.ISolutionWriter;
import at.ac.tuwien.big.moea.search.algorithm.EvolutionaryAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.LocalSearchAlgorithmFactory;
import at.ac.tuwien.big.moea.search.algorithm.provider.IRegisteredAlgorithm;
import at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension;
import at.ac.tuwien.big.momot.ModuleManager;
import at.ac.tuwien.big.momot.TransformationResultManager;
import at.ac.tuwien.big.momot.TransformationSearchOrchestration;
import at.ac.tuwien.big.momot.problem.solution.TransformationSolution;
import at.ac.tuwien.big.momot.problem.solution.variable.ITransformationVariable;
import at.ac.tuwien.big.momot.problem.solution.variable.UnitApplicationVariable;
import at.ac.tuwien.big.momot.search.fitness.EGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.IEGraphMultiDimensionalFitnessFunction;
import at.ac.tuwien.big.momot.search.fitness.dimension.AbstractEGraphFitnessDimension;
import at.ac.tuwien.big.momot.search.fitness.dimension.TransformationLengthDimension;
import at.ac.tuwien.big.momot.util.MomotUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.RuleApplication;
import org.eclipse.emf.henshin.model.Parameter;
import org.eclipse.xtext.xbase.lib.Exceptions;
import org.moeaframework.algorithm.NSGAII;
import org.moeaframework.algorithm.RandomSearch;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Population;
import org.moeaframework.util.progress.ProgressListener;

import trafochainselection.TrafochainselectionPackage;
import trafochainselection.TransformationModel;

@SuppressWarnings("all")
public class TransformationChainSearch1 {
   protected static final String INITIAL_MODEL = "problem/myNewModel.xmi";

   protected static final int SOLUTION_LENGTH = 6;

   public static void finalization() {
      System.out.println("Search finished.");
   }

   public static void initialization() {
      try {
         TrafochainselectionPackage.eINSTANCE.eClass();
         System.out.println("Search started.");
         TransformationChainModelGenerator.main(null);
      } catch(final Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }

   public static void main(final String... args) {
      initialization();
      final double start1 = System.currentTimeMillis();
      final TransformationChainSearch1 search = new TransformationChainSearch1();
      search.performSearch(INITIAL_MODEL, SOLUTION_LENGTH);
      System.out.println("Time = " + (System.currentTimeMillis() - start1) / 1000 + " seconds.");
      finalization();
   }

   protected final String[] modules = new String[] { "transformations/trafochain.henshin" };

   protected final String[] unitsToRemove = new String[] { "trafochain::transformationModule::addTransformation",
         "trafochain::transformationModule::checkHasTransformation",
         "trafochain::transformationModule::removeTransformation",
         "trafochain::transformationModule::startTransformation" };

   protected final int populationSize = 4;

   protected final int maxEvaluations = 8;

   protected final int nrRuns = 1;

   protected String baseName;

   protected double significanceLevel = 0.01;

   protected IFitnessDimension<TransformationSolution> _createConstraint_0(
         final TransformationSearchOrchestration orchestration) {
      return new AbstractEGraphFitnessDimension("chainEndsInTargetMM",
            at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
         @Override
         protected double internalEvaluate(final TransformationSolution solution) {
            final EGraph graph = solution.execute();
            final EObject root = MomotUtil.getRoot(graph);
            return _createConstraintHelper_0(solution, graph, root);
         }
      };
   }

   protected double _createConstraintHelper_0(final TransformationSolution solution, final EGraph graph,
         final EObject root) {
      return FitnessCalculator.calculateChainValidity((TransformationModel) root);
   }

   protected ProgressListener _createListener_0() {
      final SeedRuntimePrintListener _seedRuntimePrintListener = new SeedRuntimePrintListener();
      return _seedRuntimePrintListener;
   }

   protected IFitnessDimension<TransformationSolution> _createObjective_0(
         final TransformationSearchOrchestration orchestration) {
      return new AbstractEGraphFitnessDimension("TransformationCoverage",
            at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Maximum) {
         @Override
         protected double internalEvaluate(final TransformationSolution solution) {
            final EGraph graph = solution.execute();
            final EObject root = MomotUtil.getRoot(graph);
            return _createObjectiveHelper_0(solution, graph, root);
         }
      };
   }

   protected IFitnessDimension<TransformationSolution> _createObjective_1(
         final TransformationSearchOrchestration orchestration) {
      return new AbstractEGraphFitnessDimension("StructuralFeatures",
            at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum) {
         @Override
         protected double internalEvaluate(final TransformationSolution solution) {
            final EGraph graph = solution.execute();
            final EObject root = MomotUtil.getRoot(graph);
            return _createObjectiveHelper_1(solution, graph, root);
         }
      };
   }

   protected IFitnessDimension<TransformationSolution> _createObjective_2(
         final TransformationSearchOrchestration orchestration) {
      final IFitnessDimension<TransformationSolution> dimension = _createObjectiveHelper_2();
      dimension.setName("SolutionLength");
      dimension.setFunctionType(at.ac.tuwien.big.moea.search.fitness.dimension.IFitnessDimension.FunctionType.Minimum);
      return dimension;
   }

   protected double _createObjectiveHelper_0(final TransformationSolution solution, final EGraph graph,
         final EObject root) {
      try {
         return FitnessCalculator.calculateTransformationCoverage_new((TransformationModel) root);
      } catch(final Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }

   protected double _createObjectiveHelper_1(final TransformationSolution solution, final EGraph graph,
         final EObject root) {
      try {
         return FitnessCalculator.calculateStructuralFeatures((TransformationModel) root);
      } catch(final Throwable _e) {
         throw Exceptions.sneakyThrow(_e);
      }
   }

   protected IFitnessDimension<TransformationSolution> _createObjectiveHelper_2() {
      final TransformationLengthDimension _transformationLengthDimension = new TransformationLengthDimension();
      return _transformationLengthDimension;
   }

   protected IRegisteredAlgorithm<RandomSearch> _createRegisteredAlgorithm_0(
         final TransformationSearchOrchestration orchestration,
         final EvolutionaryAlgorithmFactory<TransformationSolution> moea,
         final LocalSearchAlgorithmFactory<TransformationSolution> local) {
      final IRegisteredAlgorithm<RandomSearch> _createRandomSearch = moea.createRandomSearch();
      return _createRandomSearch;
   }

   protected IRegisteredAlgorithm<NSGAII> _createRegisteredAlgorithm_1(
         final TransformationSearchOrchestration orchestration,
         final EvolutionaryAlgorithmFactory<TransformationSolution> moea,
         final LocalSearchAlgorithmFactory<TransformationSolution> local) {
      final IRegisteredAlgorithm<NSGAII> _createNSGAII = moea.createNSGAII();
      return _createNSGAII;
   }

   protected IRegisteredAlgorithm<NSGAII> _createRegisteredAlgorithm_2(
         final TransformationSearchOrchestration orchestration,
         final EvolutionaryAlgorithmFactory<TransformationSolution> moea,
         final LocalSearchAlgorithmFactory<TransformationSolution> local) {
      final IRegisteredAlgorithm<NSGAII> _createNSGAIII = moea.createNSGAIII();
      return _createNSGAIII;
   }

   protected SearchExperiment<TransformationSolution> createExperiment(
         final TransformationSearchOrchestration orchestration) {
      final SearchExperiment<TransformationSolution> experiment = new SearchExperiment<>(orchestration, maxEvaluations);
      experiment.setNumberOfRuns(nrRuns);
      experiment.addProgressListener(_createListener_0());
      return experiment;
   }

   protected IEGraphMultiDimensionalFitnessFunction createFitnessFunction(
         final TransformationSearchOrchestration orchestration) {
      final IEGraphMultiDimensionalFitnessFunction function = new EGraphMultiDimensionalFitnessFunction();
      function.addObjective(_createObjective_0(orchestration));
      function.addObjective(_createObjective_1(orchestration));
      function.addObjective(_createObjective_2(orchestration));
      function.addConstraint(_createConstraint_0(orchestration));
      return function;
   }

   protected EGraph createInputGraph(final String initialGraph, final ModuleManager moduleManager) {
      final EGraph graph = moduleManager.loadGraph(initialGraph);
      return graph;
   }

   protected ModuleManager createModuleManager() {
      final ModuleManager manager = new ModuleManager();
      for(final String module : modules) {
         manager.addModule(URI.createFileURI(new File(module).getPath().toString()).toString());
      }
      manager.removeUnits(unitsToRemove);
      return manager;
   }

   protected TransformationSearchOrchestration createOrchestration(final String initialGraph,
         final int solutionLength) {
      final TransformationSearchOrchestration orchestration = new TransformationSearchOrchestration();
      final ModuleManager moduleManager = createModuleManager();
      final EGraph graph = createInputGraph(initialGraph, moduleManager);
      orchestration.setModuleManager(moduleManager);
      orchestration.setProblemGraph(graph);
      orchestration.setSolutionLength(solutionLength);
      orchestration.setFitnessFunction(createFitnessFunction(orchestration));

      final EvolutionaryAlgorithmFactory<TransformationSolution> moea = orchestration
            .createEvolutionaryAlgorithmFactory(populationSize);
      final LocalSearchAlgorithmFactory<TransformationSolution> local = orchestration
            .createLocalSearchAlgorithmFactory();
      orchestration.addAlgorithm("Random", _createRegisteredAlgorithm_0(orchestration, moea, local));
      orchestration.addAlgorithm("NSGAII", _createRegisteredAlgorithm_1(orchestration, moea, local));
      orchestration.addAlgorithm("NSGAIII", _createRegisteredAlgorithm_2(orchestration, moea, local));

      return orchestration;
   }

   protected void deriveBaseName(final TransformationSearchOrchestration orchestration) {
      final EObject root = MomotUtil.getRoot(orchestration.getProblemGraph());
      if(root == null || root.eResource() == null || root.eResource().getURI() == null) {
         baseName = getClass().getSimpleName();
      } else {
         baseName = root.eResource().getURI().trimFileExtension().lastSegment();
      }
   }

   protected TransformationResultManager handleResults(final SearchExperiment<TransformationSolution> experiment) {
      final ISolutionWriter<TransformationSolution> solutionWriter = experiment.getSearchOrchestration()
            .createSolutionWriter();
      final IPopulationWriter<TransformationSolution> populationWriter = experiment.getSearchOrchestration()
            .createPopulationWriter();
      final TransformationResultManager resultManager = new TransformationResultManager(experiment);
      Population population;
      population = SearchResultManager.createApproximationSet(experiment, (String[]) null);
      System.out.println("- Save objectives of all algorithms to 'output/objectives/objective_values.txt'");
      SearchResultManager.saveObjectives("output/objectives/objective_values.txt", population);
      System.out.println("---------------------------");
      System.out.println("Objectives of all algorithms");
      System.out.println("---------------------------");
      System.out.println(SearchResultManager.printObjectives(population));

      population = SearchResultManager.createApproximationSet(experiment, (String[]) null);
      System.out.println("- Save solutions of all algorithms to 'output/solutions/objective_values.txt'");
      SearchResultManager.savePopulation("output/solutions/objective_values.txt", population, populationWriter);
      System.out.println("- Save solutions of all algorithms to 'output/solutions/objective_values.txt'");
      SearchResultManager.saveSolutions("output/solutions/", baseName,
            MomotUtil.asIterables(population, TransformationSolution.class), solutionWriter);

      final StringBuilder algo_solutions = new StringBuilder();
      for(final Entry<SearchExecutor, List<NondominatedPopulation>> entry : resultManager.getResults().entrySet()) {

         System.out.println(entry.getKey().getName());

         population = SearchResultManager.createApproximationSet(experiment, entry.getKey().getName());
         System.out.println(SearchResultManager.printObjectives(population) + "\n");

         population = SearchResultManager.createApproximationSet(experiment, entry.getKey().getName());
         SearchResultManager.saveObjectives("output/objectives/" + entry.getKey().getName() + ".txt", population);

         algo_solutions.append("----------------------------------------------------------------------\n");
         algo_solutions.append(entry.getKey().getName());
         algo_solutions.append("\n\n----------------------------------------------------------------------");

         int noSolutions = 1;
         for(final TransformationSolution ts : MomotUtil.asIterables(population, TransformationSolution.class)) {
            algo_solutions.append("\nSolution " + noSolutions++ + ":\n");
            for(final ITransformationVariable tv : ts.getVariables()) {
               if(tv instanceof UnitApplicationVariable) {
                  final UnitApplicationVariable uav = (UnitApplicationVariable) tv;
                  algo_solutions.append(uav.getAssignment().toString());
                  for(final RuleApplication ra : uav.getAppliedRules()) {
                     algo_solutions.append(String.format("\tRule '%s'\n", ra.getUnit().getName()));
                     for(final Parameter p : ra.getResultMatch().getUnit().getParameters()) {
                        final Object o = ra.getResultParameterValue(p.getName());
                        algo_solutions
                              .append(String.format("\t\t- parameter '%s' => '%s'\n", p.getName(), o.toString()));
                     }

                  }
               }
            }
            algo_solutions.append("\nObjectives: " + Arrays.toString(ts.getObjectives()));

         }
         algo_solutions.append("\n\n");

      }

      BufferedWriter writer = null;
      try {
         writer = new BufferedWriter(new FileWriter("output/solutions/units_and_rules.txt", false));
         writer.append(algo_solutions);
         writer.close();
      } catch(final IOException e) {
         e.printStackTrace();
      }

      population = SearchResultManager.createApproximationSet(experiment, (String[]) null);
      System.out.println("- Save models of all algorithms to 'output/models/'");
      TransformationResultManager.saveModels("output/models/", baseName, population);

      return resultManager;
   }

   protected SearchAnalyzer performAnalysis(final SearchExperiment<TransformationSolution> experiment) {
      final SearchAnalysis analysis = new SearchAnalysis(experiment);
      analysis.setHypervolume(true);
      analysis.setGenerationalDistance(true);
      analysis.setShowAggregate(true);
      analysis.setShowIndividualValues(true);
      analysis.setShowStatisticalSignificance(true);
      analysis.setSignificanceLevel(significanceLevel);
      final SearchAnalyzer searchAnalyzer = analysis.analyze();
      System.out.println("---------------------------");
      System.out.println("Analysis Results");
      System.out.println("---------------------------");
      searchAnalyzer.printAnalysis();
      System.out.println("---------------------------");
      try {
         System.out.println("- Save Analysis to 'output/analysis/analysis.txt'");
         searchAnalyzer.saveAnalysis(new File("output/analysis/analysis.txt"));
      } catch(final IOException e) {
         e.printStackTrace();
      }
      System.out.println("- Save Indicator BoxPlots to 'output/analysis/'");
      searchAnalyzer.saveIndicatorBoxPlots("output/analysis/", baseName);
      return searchAnalyzer;
   }

   public void performSearch(final String initialGraph, final int solutionLength) {
      final TransformationSearchOrchestration orchestration = createOrchestration(initialGraph, solutionLength);
      deriveBaseName(orchestration);
      printSearchInfo(orchestration);
      final SearchExperiment<TransformationSolution> experiment = createExperiment(orchestration);
      experiment.run();
      System.out.println("-------------------------------------------------------");
      System.out.println("Analysis");
      System.out.println("-------------------------------------------------------");
      performAnalysis(experiment);
      System.out.println("-------------------------------------------------------");
      System.out.println("Results");
      System.out.println("-------------------------------------------------------");
      handleResults(experiment);
   }

   public void printSearchInfo(final TransformationSearchOrchestration orchestration) {
      System.out.println("-------------------------------------------------------");
      System.out.println("Search");
      System.out.println("-------------------------------------------------------");
      System.out.println("InputModel:      " + INITIAL_MODEL);
      System.out.println("Objectives:      " + orchestration.getFitnessFunction().getObjectiveNames());
      System.out.println("NrObjectives:    " + orchestration.getNumberOfObjectives());
      System.out.println("Constraints:     " + orchestration.getFitnessFunction().getConstraintNames());
      System.out.println("NrConstraints:   " + orchestration.getNumberOfConstraints());
      System.out.println("Transformations: " + Arrays.toString(modules));
      System.out.println("Units:           " + orchestration.getModuleManager().getUnits());
      System.out.println("SolutionLength:  " + orchestration.getSolutionLength());
      System.out.println("PopulationSize:  " + populationSize);
      System.out.println("Iterations:      " + maxEvaluations / populationSize);
      System.out.println("MaxEvaluations:  " + maxEvaluations);
      System.out.println("AlgorithmRuns:   " + nrRuns);
      System.out.println("---------------------------");
   }
}
