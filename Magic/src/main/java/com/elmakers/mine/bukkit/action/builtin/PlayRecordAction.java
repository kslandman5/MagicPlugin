package com.elmakers.mine.bukkit.action.builtin;

import com.elmakers.mine.bukkit.action.BaseSpellAction;
import com.elmakers.mine.bukkit.api.action.CastContext;
import com.elmakers.mine.bukkit.api.magic.MaterialSet;
import com.elmakers.mine.bukkit.api.spell.SpellResult;
import com.google.common.collect.Lists;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.Random;

public class PlayRecordAction extends BaseSpellAction
{
    private String recordList = "";

    @Override
    public void prepare(CastContext context, ConfigurationSection parameters)
    {
        super.prepare(context, parameters);
        recordList = parameters.getString("records", "records");
    }
	
	@SuppressWarnings("deprecation")
	@Override
    public SpellResult perform(CastContext context) {
        MaterialSet recordSet = context.getMaterialSet(recordList);
        if (recordSet == null) {
            return SpellResult.FAIL;
        }

        List<Material> records = Lists.newArrayList(recordSet.getMaterials());
        if (records.size() == 0) {
            return SpellResult.FAIL;
        }

        Random random = context.getRandom();
        Material record = records.get(random.nextInt(records.size()));

        Location location = context.getTargetLocation();
        location.getWorld().playEffect(location, Effect.RECORD_PLAY,
                record.getId());

        return SpellResult.CAST;
    }

    @Override
    public boolean requiresTarget() {
        return true;
    }
}
