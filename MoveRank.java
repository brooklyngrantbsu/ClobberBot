public class MoveRank {
	public ClobberBotAction move; 
	public int rank;
	
	public MoveRank(ClobberBotAction move, int rank) {
		this.move = move;
		this.rank = rank;
	}
	
	public int getRank() {
		return rank;
	}

}
