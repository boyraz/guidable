package com.example.ui;

public final class DBTables {
	public static final class booth{
		public static final String ID = "_id";
		public static final String NAME = "name";
		public static final String LocationX = "location_x";
		public static final String LocationY = "location_y";
		public static final String DESCRIPTION = "description";
        public static final String NumOfVisit = "num_of_visit";
        public static final String RECOMMENDATION = "recommendation";
        public static final String POPULAR = "popular";
        public static final String EVENT = "event";
		public static final String _TABLENAME = "booth";
		public static final String _CREATE =
				"create table " + _TABLENAME + "("
				+ ID + " integer primary key, "
				+ NAME + " text not null, "
				+ LocationX + " integer not null, "
				+ LocationY + " integer not null, "
                + NumOfVisit + " integer not null, "
                + RECOMMENDATION + " integer not null, "
                + POPULAR + " integer not null, "
                + EVENT + " integer not null, "
				+ DESCRIPTION + " text not null);";
	}
	public static final class history{
		public static final String ID = "_id";
		public static final String TIME = "time";
		public static final String BID = "booth_id";
		public static final String DURATION = "duration";
		public static final String _TABLENAME = "history";
		public static final String _CREATE =
                "create table " + _TABLENAME + "("
                + ID + " integer primary key autoincrement, "
				+ TIME + " integer not null, "
				+ BID + " integer not null, "
				+ DURATION + " integer not null, "
				+ "FOREIGN KEY(" + BID + ") REFERENCES "
				+ booth._TABLENAME + "(" + booth.ID + "));";
	}

	public static final class beacon{
		public static final String MINOR = "minor";
		public static final String BID = "booth_id";
		public static final String LocationX = "location_x";
		public static final String LocationY = "location_y";
		public static final String RANGE = "range";
		public static final String _TABLENAME = "beacon";
		public static final String _CREATE =
				"create table " + _TABLENAME + "("
				+ MINOR + " integer primary key, "
				+ BID + " integer not null, "
				+ LocationX + " integer not null, "
				+ LocationY + " integer not null, "
				+ RANGE + " real not null, "
				+ "FOREIGN KEY(" + BID + ") REFERENCES "
				+ booth._TABLENAME + "(" + booth.ID + "));";
	}
}
