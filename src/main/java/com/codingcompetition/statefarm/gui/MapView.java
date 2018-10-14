package com.codingcompetition.statefarm.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import com.codingcompetition.statefarm.SearchCriteria;
import com.codingcompetition.statefarm.StreetMapDataInterpreter;
import com.codingcompetition.statefarm.gui.MainPanel.SearchCriteriaListener;
import com.codingcompetition.statefarm.model.PointOfInterest;

public class MapView extends JPanel implements SearchCriteriaListener {

	private static final long serialVersionUID = 1L;

	private StreetMapDataInterpreter interpreter;
	
	private BufferedImage background;
	
	private BufferedImage marker;

	private final Object filteredPointsLock;
	
	private List<PointOfInterest> filteredPoints;
	
	private double minLat, minLon, maxLat, maxLon;
	
	public MapView(StreetMapDataInterpreter interpreter) {
		this.interpreter = interpreter;
		this.filteredPointsLock = new Object();
		this.filteredPoints = this.interpreter.interpret();
		
		try {
			marker = ImageIO.read(loadRes("/marker.png"));
			
			if (interpreter.getParser().getFileName().contains("small-metro")) {
				// Bloomington
				background = ImageIO.read(loadRes("/map_bloomington.png"));
			} else {
				// Chicago
				background = ImageIO.read(loadRes("/map_chicago.png"));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		minLat = interpreter.getParser().getMinLat();
		minLon = interpreter.getParser().getMinLong();
		maxLat = interpreter.getParser().getMaxLat();
		maxLon = interpreter.getParser().getMaxLong();
	}
	
	@Override
	public void paint(Graphics g) {
		g.drawImage(background, 0, 0, this.getWidth(), this.getHeight(), null);
		
		int markerWidth, markerHeight;
		
		// Draw each point of interest
		synchronized (filteredPointsLock) {
			if (filteredPoints.size() < 50) {
				markerWidth = 15;
			} else {
				markerWidth = 5;
			}
			markerHeight = (int) (marker.getHeight() * 1.0 / marker.getWidth() * markerWidth);
				
			for (PointOfInterest point : filteredPoints) {
				double latPercent = (Double.parseDouble(point.getLatitude()) - minLat) / (maxLat - minLat);
				double lonPercent = (Double.parseDouble(point.getLongitude()) - minLon) / (maxLon - minLon);
				int xLoc = (int) (this.getWidth() * latPercent);
				int yLoc = (int) (this.getHeight() * (1 - lonPercent));
				g.drawImage(marker, xLoc - markerWidth / 2, yLoc - markerHeight, markerWidth, markerHeight, null);
			}
		}
	}

	@Override
	public void onSearchCriteriaChange(List<SearchCriteria> criteria) {
		Map<Integer, SearchCriteria> priorityMap = new HashMap<>();
		for (int i = 0; i < criteria.size(); i++) {
			priorityMap.put(i, criteria.get(i));
		}
		synchronized (filteredPointsLock) {
			this.filteredPoints = this.interpreter.interpret(priorityMap);	
		}
		this.repaint();
	}
	
	private File loadRes(String name) {
		return new File(MapView.class.getResource(name).getFile());
	}
}
