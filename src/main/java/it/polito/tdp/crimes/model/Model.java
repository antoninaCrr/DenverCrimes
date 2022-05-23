package it.polito.tdp.crimes.model;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import it.polito.tdp.crimes.db.EventsDao;

public class Model {
	
	private Graph<String, DefaultWeightedEdge> grafo; // i vertici sono delle Stringhe, gli archi sono pesati
	private EventsDao dao;
	
	private List<String> best;
	
	public Model() {
		dao = new EventsDao();
	}
	
	public void creaGrafo(String categoria, int mese) {
		grafo = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
	
		// aggiunta vertici
		Graphs.addAllVertices(this.grafo, dao.getVertici(categoria, mese));
		
		// aggiunta archi
		for(Adiacenza a : dao.getArchi(categoria, mese)) {
			// sono sicura che i vertici siano presenti nel grafo in quanto nella query ho impostato le medesime condizioni di where
			Graphs.addEdgeWithVertices(this.grafo, a.getV1(), a.getV2(),a.getPeso());
		}
		
		// fare le stampe di prova per verificare la correttezza
					System.out.println("Grafo creato!");
					System.out.println("# VERTICI: "+this.grafo.vertexSet().size());
					System.out.println("# ARCHI: "+this.grafo.edgeSet().size());
	}
	
	public int nVertici() {
		return this.grafo.vertexSet().size();
	}
	
	public int nArchi() {
		return this.grafo.edgeSet().size();
	}
	
	public List<Adiacenza> getArchi(){
		List<Adiacenza> archi = new ArrayList<Adiacenza>();
		for (DefaultWeightedEdge e : this.grafo.edgeSet()) {
			archi.add(new Adiacenza(this.grafo.getEdgeSource(e),
						this.grafo.getEdgeTarget(e), 
						(int) this.grafo.getEdgeWeight(e)));
		}
		return archi;
	}
	
	public List<String> getCategorie(){
		return this.dao.getCategorie();
	}
	
	public List<Adiacenza> getArchiMaggioriPesoMedio(){
		// scorro gli archi del grafo e calcolo il peso medio
		double pesoTot = 0.0;
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			pesoTot += this.grafo.getEdgeWeight(e);
		}
		
		double avg = pesoTot/this.grafo.edgeSet().size();
		System.out.println("PESO MEDIO: "+avg);
		// riscorrso tutti gli archi prendendo quelli maggiori di avg
		List<Adiacenza> result = new ArrayList<>();
		for(DefaultWeightedEdge e : this.grafo.edgeSet()) {
			if(this.grafo.getEdgeWeight(e)>avg) {
				result.add(new Adiacenza(this.grafo.getEdgeSource(e),this.grafo.getEdgeTarget(e),(int)(this.grafo.getEdgeWeight(e))));
			}
		}
		return result;
	}
	// NOTA: è importante conoscere almeno i metodi elementari per muoversi dentro un grafo

	// cammino migliore = cammino contenente il max num di archi

	public List<String> calcolaPercorso(String sorgente, String destinazione){
		best = new LinkedList<>();
		List<String> parziale = new LinkedList<>();
		// sappiamo esattamente da dove partiamo per calcolare il percorso
		parziale.add(sorgente);
		cerca(parziale, destinazione); // abbiamo già messo il primo passo del percorso (NON utilizziamo il livello)
		return best;
	}
	
	private void cerca(List<String> parziale, String destinazione ) {
		// condizione di terminazione
		if(parziale.get(parziale.size()-1).equals(destinazione)) {// sono arrivato al termine
		   // è la soluzione migliore?
			if(parziale.size()>best.size()) {
				best = new LinkedList<>(parziale); // SOVRASCRIVO FACENDO UNA NEW	
			}
				return;
		}
		
		// scorro i vicini dell'ultimo inserito e provo le varie "strade"
		for(String v: Graphs.neighborListOf(this.grafo, parziale.get(parziale.size()-1))) {
			// neighborListOf mi restituisce la lista contenente i vicini del vertice passato come secondo param.
			if(!parziale.contains(v)) { // condizione per evitare percorsi ciclici
			parziale.add(v);
			cerca(parziale, destinazione);
			parziale.remove(parziale.size()-1);
			}
		}
	}
}
