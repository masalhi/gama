/*
 * GAMA - V1.4 http://gama-platform.googlecode.com
 * 
 * (c) 2007-2011 UMI 209 UMMISCO IRD/UPMC & Partners (see below)
 * 
 * Developers :
 * 
 * - Alexis Drogoul, UMI 209 UMMISCO, IRD/UPMC (Kernel, Metamodel, GAML), 2007-2012
 * - Vo Duc An, UMI 209 UMMISCO, IRD/UPMC (SWT, multi-level architecture), 2008-2012
 * - Patrick Taillandier, UMR 6228 IDEES, CNRS/Univ. Rouen (Batch, GeoTools & JTS), 2009-2012
 * - Beno�t Gaudou, UMR 5505 IRIT, CNRS/Univ. Toulouse 1 (Documentation, Tests), 2010-2012
 * - Phan Huy Cuong, DREAM team, Univ. Can Tho (XText-based GAML), 2012
 * - Pierrick Koch, UMI 209 UMMISCO, IRD/UPMC (XText-based GAML), 2010-2011
 * - Romain Lavaud, UMI 209 UMMISCO, IRD/UPMC (RCP environment), 2010
 * - Francois Sempe, UMI 209 UMMISCO, IRD/UPMC (EMF model, Batch), 2007-2009
 * - Edouard Amouroux, UMI 209 UMMISCO, IRD/UPMC (C++ initial porting), 2007-2008
 * - Chu Thanh Quang, UMI 209 UMMISCO, IRD/UPMC (OpenMap integration), 2007-2008
 */
package msi.gama.metamodel.topology.graph;

import java.util.*;
import msi.gama.metamodel.agent.IAgent;
import msi.gama.metamodel.shape.*;
import msi.gama.metamodel.topology.*;
import msi.gama.metamodel.topology.filter.IAgentFilter;
import msi.gama.runtime.IScope;
import msi.gama.runtime.exceptions.GamaRuntimeException;
import msi.gama.util.*;
import msi.gama.util.graph.IGraph;
import msi.gaml.operators.Maths;

/**
 * The class GraphTopology.
 * 
 * @author drogoul
 * @since 27 nov. 2011
 * 
 */
public class GraphTopology extends AbstractTopology {

	/**
	 * @param scope
	 * @param env
	 * @param torus
	 */
	public GraphTopology(final IScope scope, final IShape env, final GamaSpatialGraph graph) {
		super(scope, env);
		places = graph;
	}

	@Override
	protected boolean createAgents() {
		return true;
	}

	/**
	 * @throws GamaRuntimeException
	 * @see msi.gama.environment.ITopology#pathBetween(msi.gama.interfaces.IGeometry,
	 *      msi.gama.interfaces.IGeometry)
	 */
	@Override
	public IPath pathBetween(final IShape source, final IShape target) {
		IShape s1 = null;
		IShape t1 = null;
		IShape s2 = null;
		IShape t2 = null;

		IShape edgeS = null, edgeT = null;
		double dist1 = Double.MAX_VALUE;
		double dist2 = Double.MAX_VALUE;

		for ( Object o : getPlaces().getEdges() ) {
			IShape eg = (IShape) o;
			// try {
			// eg = Cast.asGeometry(scope, o);
			double d1 = eg.euclidianDistanceTo(source);
			if ( d1 < dist1 ) {
				edgeS = eg;
				s1 = (IShape) getPlaces().getEdgeSource(o);
				s2 = (IShape) getPlaces().getEdgeTarget(o);
				dist1 = d1;
			}
			double d2 = eg.euclidianDistanceTo(target);
			if ( d2 < dist2 ) {
				edgeT = eg;
				t1 = (IShape) getPlaces().getEdgeSource(o);
				t2 = (IShape) getPlaces().getEdgeTarget(o);
				dist2 = d2;
			}
			// } catch (GamaRuntimeException e) {
			// return null;
			// }
		}
		IPath pathComplete = null;
		if ( edgeS == edgeT ) {
			pathComplete = new GamaPath(this, source, target, GamaList.with(edgeS));
		} else {
			IShape nodeT = t1;
			// try {
			// IShape g1 = t1; // Cast.asGeometry(scope, t1);
			// IShape g2 = t2; // Cast.asGeometry(scope, t2);
			if ( t1.euclidianDistanceTo(target) > t2.euclidianDistanceTo(target) ) {
				nodeT = t2;
			}
			// } catch (GamaRuntimeException e) {
			// return null;
			// }
			Object nodeS = s1;
			// try {
			// g1 = s1; // Cast.asGeometry(scope, s1);
			// g2 = s2; // Cast.asGeometry(scope, s2);
			if ( s1 == nodeT || s2 != nodeT &&
				s1.euclidianDistanceTo(source) > s2.euclidianDistanceTo(source) ) {
				nodeS = s2;
			}
			// } catch (GamaRuntimeException e) {
			// return null;
			// }

			GamaPath path = (GamaPath) getPlaces().computeShortestPathBetween(this, nodeS, nodeT);
			IList edges = new GamaList(path.getEdgeList());
			if ( edges.isEmpty() ) { return null; }
			HashSet edgesSetInit =
				new HashSet(Arrays.asList(((IShape) edges.get(0)).getInnerGeometry()
					.getCoordinates()));
			HashSet edgesSetS =
				new HashSet(Arrays.asList(edgeS.getInnerGeometry().getCoordinates()));
			if ( !edgesSetS.equals(edgesSetInit) ) {
				edges.add(0, edgeS);
			}
			HashSet edgesSetEnd =
				new HashSet(Arrays.asList(((IShape) edges.get(edges.size() - 1)).getInnerGeometry()
					.getCoordinates()));
			HashSet edgesSetT =
				new HashSet(Arrays.asList(edgeT.getInnerGeometry().getCoordinates()));

			if ( !edgesSetT.equals(edgesSetEnd) ) {
				edges.add(edgeT);
			}
			pathComplete = new GamaPath(this, source, target, edges);
		}
		return pathComplete;
	}

	/**
	 * @see msi.gama.interfaces.IValue#stringValue()
	 */
	@Override
	public String stringValue() throws GamaRuntimeException {
		return "GraphTopology";
	}

	/**
	 * @see msi.gama.environment.AbstractTopology#_toGaml()
	 */
	@Override
	protected String _toGaml() {
		return "GraphTopology";
	}

	/**
	 * @see msi.gama.environment.AbstractTopology#_copy()
	 */
	@Override
	protected ITopology _copy() {
		return new GraphTopology(scope, environment, (GamaSpatialGraph) places);
	}

	/**
	 * @see msi.gama.environment.AbstractTopology#getRandomPlace()
	 */

	@Override
	public IGraph<IShape, IShape> getPlaces() {
		return (GamaSpatialGraph) super.getPlaces();
	}

	/**
	 * @see msi.gama.environment.ITopology#isValidLocation(msi.gama.util.GamaPoint)
	 */
	@Override
	public boolean isValidLocation(final ILocation p) {
		return isValidGeometry(p.getGeometry());
	}

	/**
	 * @see msi.gama.environment.ITopology#isValidGeometry(msi.gama.interfaces.IGeometry)
	 */
	@Override
	public boolean isValidGeometry(final IShape g) {
		// Geometry g2 = g.getInnerGeometry();
		for ( IShape g1 : places ) {
			if ( g1.intersects(g) ) { return true; }
			// TODO covers or intersects ?
		}
		return false;
	}

	/**
	 * @see msi.gama.environment.ITopology#distanceBetween(msi.gama.interfaces.IGeometry,
	 *      msi.gama.interfaces.IGeometry, java.lang.Double)
	 */
	@Override
	public Double distanceBetween(final IShape source, final IShape target) {
		IPath path = this.pathBetween(source, target);
		if ( path == null ) { return Double.MAX_VALUE; }
		return path.getDistance();
		// return d <= max_distance ? d : null;
	}

	/**
	 * @see msi.gama.environment.ITopology#directionInDegreesTo(msi.gama.interfaces.IGeometry,
	 *      msi.gama.interfaces.IGeometry)
	 */
	@Override
	public Integer directionInDegreesTo(final IShape source, final IShape target) {
		IPath path = this.pathBetween(source, target);
		if ( path == null ) { return null; }
		// LineString ls = (LineString) path.getEdgeList().first().getInnerGeometry();
		// TODO Check this
		final double dx = target.getLocation().getX() - source.getLocation().getX();
		final double dy = target.getLocation().getY() - source.getLocation().getY();
		final double result = Maths.aTan2(dy, dx) * Maths.toDeg;
		return Maths.checkHeading((int) result);
	}

	/**
	 * @see msi.gama.environment.ITopology#getAgentsIn(msi.gama.interfaces.IGeometry,
	 *      msi.gama.environment.IAgentFilter, boolean)
	 */
	@Override
	public IList<IAgent> getAgentsIn(final IShape source, final IAgentFilter f,
		final boolean covered) {
		List<IAgent> agents = super.getAgentsIn(source, f, covered);
		GamaList<IAgent> result = new GamaList();
		for ( IAgent ag : agents ) {
			if ( isValidGeometry(ag) ) {
				result.add(ag);
			}
		}
		return result;
	}

}
