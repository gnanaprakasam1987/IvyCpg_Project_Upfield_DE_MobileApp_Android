package com.ivy.sd.png.bo;

public class ShelfShareBO {

	/**
	 * Contains name of which product currently filled in shelf.
	 */
	private String topLeftCell = null;
	/**
	 * Contains name of which product currently filled in shelf.
	 */
	private String topRightCell = null;
	/**
	 * Contains name of which product currently filled in shelf.
	 */
	private String bottomLeftCell = null;
	/**
	 * Contains name of which product currently filled in shelf.
	 */
	private String bottomRightCell = null;

	/**
	 * 0 - for fully filled by some brand, 1 - for fully filled by others
	 */
	private int fullyFilledByOthers = 1;

	public void setFirstCell(String topLeftCell) {
		this.topLeftCell = topLeftCell;
	}

	public String getFirstCell() {
		return topLeftCell;
	}

	public void setSecondCell(String topRightCell) {
		this.topRightCell = topRightCell;
	}

	public String getSecondCell() {
		return topRightCell;
	}

	public void setThirdCell(String bottomLeftCell) {
		this.bottomLeftCell = bottomLeftCell;
	}

	public String getThirdCell() {
		return bottomLeftCell;
	}

	public void setFourthCell(String bottomRightCell) {
		this.bottomRightCell = bottomRightCell;
	}

	public String getFourthCell() {
		return bottomRightCell;
	}

	public void setOthersCount(int othersCount) {
		this.fullyFilledByOthers = othersCount;
	}

	public int getOthersCount() {
		return fullyFilledByOthers;
	}

}
