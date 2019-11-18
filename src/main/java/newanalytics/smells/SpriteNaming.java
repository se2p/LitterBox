/*
 * Copyright (C) 2019 LitterBox contributors
 *
 * This file is part of LitterBox.
 *
 * LitterBox is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * LitterBox is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LitterBox. If not, see <http://www.gnu.org/licenses/>.
 */
package newanalytics.smells;

import newanalytics.IssueFinder;
import newanalytics.IssueReport;
import scratch.newast.model.Program;

/**
 * Checks if all Sprites have different names.
 */
public class SpriteNaming implements IssueFinder {

    private String note1 = "All sprites have a different name and dont start with 'Sprite'.";
    private String note2 = "Some sprites have the same name or start with 'Sprite'.";
    private String name = "sprite_naming";

    @Override
    public IssueReport check(Program program) {
        /*
        List<Scriptable> scriptables = new ArrayList<>(program.getSprites());
        List<String> pos = new ArrayList<>();
        for (Scriptable scable : scriptables) {
            String name = scable.getName();
            if (name.startsWith("Sprite")) {
                pos.add(scable.getName());
                continue;
            }
            for (Scriptable sc2 : scriptables) {
                if (!sc2.equals(scable)) {
                    if (sc2.getName().replaceAll("[0-9]", "").equals(scable.getName().replaceAll("[0-9]", ""))) {
                        pos.add(scable.getName());
                        break;
                    }
                }
            }
        }
        String notes;
        if (pos.size() > 0) {
            notes = note2;
        } else {
            notes = note1;
        }
        return new IssueReport(name, pos.size(), pos, program.getPath(), notes);

         */
        throw new RuntimeException("not implemented");
    }

    public String getNote1() {
        return note1;
    }

    public void setNote1(String note1) {
        this.note1 = note1;
    }

    public String getNote2() {
        return note2;
    }

    public void setNote2(String note2) {
        this.note2 = note2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
