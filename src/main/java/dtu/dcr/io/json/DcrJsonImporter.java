package dtu.dcr.io.json;

import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import dtu.dcr.engine.Activity;
import dtu.dcr.engine.Relation.TYPES;

public class DcrJsonImporter {

	public static dtu.dcr.engine.Process importProcess(String json) {
		dtu.dcr.engine.Process p = new dtu.dcr.engine.Process();
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		Relations r = gson.fromJson(json, Relations.class);

		for (Relation rel : r.Relation) {
			Activity source = p.getActivityFromName(rel.source);
			if (source == null) {
				source = p.addActivity(rel.source);
			}
			Activity target = p.getActivityFromName(rel.target);
			if (target == null) {
				target = p.addActivity(rel.target);
			}
			p.addRelation(source, TYPES.valueOf(rel.type.toUpperCase()), target);
		}

		return p;
	}
}

class Relations {
	public Set<Relation> Relation = new HashSet<>();
}

class Relation {
	public String type;
	public String source;
	public String target;
}