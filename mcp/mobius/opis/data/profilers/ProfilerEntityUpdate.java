package mcp.mobius.opis.data.profilers;

import java.util.HashMap;
import java.util.WeakHashMap;

import net.minecraft.entity.Entity;

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import mcp.mobius.mobiuscore.profiler.IProfilerBase;
import mcp.mobius.opis.modOpis;
import mcp.mobius.opis.data.profilers.Clock.IClock;

public class ProfilerEntityUpdate extends ProfilerAbstract implements IProfilerBase {

	private IClock clock = Clock.getNewClock();
	public  WeakHashMap<Entity, DescriptiveStatistics> data = new WeakHashMap<Entity, DescriptiveStatistics>();
	
	@Override
	public void reset() {
		this.data = new WeakHashMap<Entity, DescriptiveStatistics>();
	}	
	
	@Override
	public void start(Object key) {
		Entity entity = (Entity)key;
		if (entity.worldObj.isRemote) return;
		
		if (!data.containsKey(entity))
			data.put(entity, new DescriptiveStatistics());
		clock.start();
	}
	
	@Override
	public void stop(Object key) {
		Entity entity = (Entity)key;
		if (entity.worldObj.isRemote) return;
		
		clock.stop();
		try{
			data.get(entity).addValue((double)clock.getDelta());
		} catch (Exception e){
			modOpis.log.warning(String.format("Error while profiling entity %s\n", key));
		}
	}
}
