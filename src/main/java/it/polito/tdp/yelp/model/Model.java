package it.polito.tdp.yelp.model;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

import it.polito.tdp.yelp.db.YelpDao;

public class Model {
	
	YelpDao dao;
	Graph <Review,DefaultWeightedEdge> grafo;
	List<Review> best;
	
	public Model() {
		dao = new YelpDao();
		
	}
	
	public List<String> getAllCities(){
		
		List<String> citta = new ArrayList<String>();
		List<Business> business = new ArrayList<Business>(this.dao.getAllBusiness());
		
		for(Business b : business) {
			if(!citta.contains(b.getCity())) {
			citta.add(b.getCity());
			}
		}
		
		Collections.sort(citta);
		
		return citta;
	}
	
	public List<Business> getAllCityBusinesses(String citta){
		List<Business> business = new ArrayList<Business>(this.dao.getAllBusiness());
		List<Business> businessCitta = new ArrayList<Business>();
		
		for(Business b : business) {
			if(b.getCity().compareTo(citta)==0) {
				businessCitta.add(b);
			}
		}
		
		Collections.sort(businessCitta);
		
		return businessCitta;
	}
	
	
	public void creaGrafo(Business b) {
		this.grafo = new SimpleDirectedWeightedGraph<Review,DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		List<Review> reviews = new ArrayList<Review>(this.dao.getAllReviews(b));
		
		Graphs.addAllVertices(this.grafo, reviews);
		
		for(Review r1 : reviews) {
			for(Review r2 : reviews) {
				if(r1.getDate().isBefore(r2.getDate())) {
					if(ChronoUnit.DAYS.between(r1.getDate(), r2.getDate())!=0) {
					Graphs.addEdgeWithVertices(this.grafo, r1, r2, Math.abs(ChronoUnit.DAYS.between(r1.getDate(), r2.getDate())));
					}
				} 
				
			}
		}
		
	}
	
	public String getMaxReview() {
		int max = 0;
		Review review = null;
		
		for(Review r : this.grafo.vertexSet()) {
			if(max<this.grafo.outgoingEdgesOf(r).size()) {
				max = this.grafo.outgoingEdgesOf(r).size();
				review = r;
			}
		}
		
		
		return "Grafo Creato, #VERTICI: " + this.grafo.vertexSet().size() + ", #ARCHI: " + this.grafo.edgeSet().size() + "\n" + review.getReviewId() + " #ARCHIUSCENTI: " + this.grafo.outgoingEdgesOf(review).size();
		
	}
	
	
	public List<Review> ricercaCammino() {
		List<Review> parziale = new ArrayList<Review>();
		best = new ArrayList<Review>();
		
		for(Review r : this.grafo.vertexSet()) {
			
			parziale.add(r);
			cerca(parziale, r.getStars());
		}
		
		
		return best;
	}

	private void cerca(List<Review> parziale, double stars) {

		if(parziale.size()>best.size()) {
			best = new ArrayList<Review>(parziale);
		}
		
		for(DefaultWeightedEdge e : this.grafo.outgoingEdgesOf(parziale.get(parziale.size()-1))) {
			if(this.grafo.getEdgeTarget(e).getStars()>=parziale.get(parziale.size()-1).getStars()) {
				if(!parziale.contains(this.grafo.getEdgeTarget(e))) {
					parziale.add(this.grafo.getEdgeTarget(e));
					cerca(parziale,this.grafo.getEdgeTarget(e).getStars());
					parziale.remove(parziale.size()-1);
				}
			}
		}
		
		
	}
	
	
}
