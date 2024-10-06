import org.opencv.ximgproc.FastLineDetector;

import java.awt.*;
import java.awt.image.*;
import java.util.*;


public class RegionFinder {
	private static final int maxColorDiff = 20;                // how similar a pixel color must be to the target color, to belong to a region
	private static final int minRegion = 50;                // how many points in a region to be worth considering
	private BufferedImage image;                            // the image in which to find regions
	private BufferedImage recoloredImage;                   // the image with identified regions recolored

	private Color trackColor = null;            // point-tracking target color

	private ArrayList<ArrayList<Point>> regions;            // a region is a list of points
	// so the identified regions are in a list of lists of points

	public RegionFinder() {
		this.image = null;
	}

	public RegionFinder(BufferedImage image) {
		this.image = image;
	}

	public void setImage(BufferedImage image) {
		this.image = image;
	}

	public BufferedImage getImage() {
		return image;
	}

	public BufferedImage getRecoloredImage() {
		return recoloredImage;
	}


	/**
	 * Sets regions to the flood-fill regions in the image, similar enough to the trackColor.
	 */
	public void findRegions(Color targetColor) {
		regions = new ArrayList<>();
		int width = image.getWidth();
		int height = image.getHeight();
		BufferedImage visited = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < image.getHeight(); y++) {  //loops through every pixel
			for (int x = 0; x < image.getWidth(); x++) {
				Color c = new Color(image.getRGB(x, y));

				if ((visited.getRGB(x, y) == 0) && (colorMatch(c, targetColor))) {
					ArrayList<Point> region = new ArrayList<>();
					ArrayList<Point> toVisit = new ArrayList<>();
					toVisit.add(new Point(x, y)); //adds the point toVisit, if the color is black, meaning its not visited

					while (toVisit.size() > 0) {  //if there are still points in the list
						Point p = toVisit.get(0);
						region.add(p);  //adds the point to the region
						visited.setRGB(p.x, p.y, 1);

						for (int ny = Math.max(0, p.y - 1); ny < Math.min(image.getHeight(), p.y + 2); ny++) { //looks at the neighboring pixels
							for (int nx = Math.max(0, p.x - 1); nx < Math.min(image.getWidth(), p.x + 2); nx++) {

								if (nx != p.x && ny != p.y) { //if the negihboring pixels are not in the same position as the points x, y
									Color g = new Color(image.getRGB(nx, ny));

									if ((colorMatch(g, targetColor)) && visited.getRGB(nx, ny) == 0) {
										toVisit.add(new Point(nx, ny));
										visited.setRGB(nx, ny, 1);
									}
								}
							}
						}
						toVisit.remove(p);
					}
					if (region.size() > minRegion) {
						regions.add(region);
					}
				}
			}
		}


	}


	/**
	 * Tests whether the two colors are "similar enough" (your definition, subject to the maxColorDiff threshold, which you can vary).
	 */
	private static boolean colorMatch(Color c1, Color c2) {
		int rdiff = Math.abs(c1.getRed() - c2.getRed());
		int gdiff = Math.abs(c1.getGreen() - c2.getGreen());
		int bdiff = Math.abs(c1.getBlue() - c2.getBlue());
		if (rdiff > maxColorDiff || gdiff > maxColorDiff || bdiff > maxColorDiff) {
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Returns the largest region detected (if any region has been detected)
	 */
	public ArrayList<Point> largestRegion() {
		ArrayList<Point> biggestRegion = null;
		if (!regions.isEmpty()) {
			biggestRegion = regions.get(0);
			for (ArrayList<Point> region : regions) {
				if (region.size() > biggestRegion.size()) {
					biggestRegion = region;
				}
			}
		}
		return biggestRegion;
		}


	/**
	 * Sets recoloredImage to be a copy of image,
	 * but with each region a uniform random color,
	 * so we can see where they are
	 */
	public void recolorImage() {
		// First copy the original
		recoloredImage = new BufferedImage(image.getColorModel(), image.copyData(null), image.getColorModel().isAlphaPremultiplied(), null);
		// Now recolor the regions in it
		for (ArrayList<Point> region : regions) {
			Color newColor = new Color((int)(Math.random()*16777216));
			for (Point p : region) {
				recoloredImage.setRGB(p.x, p.y, newColor.getRGB());

			}
		}
	}
}
