package com.melot.kkgame.redis.support;



import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Tuple;

import com.melot.kktv.util.redis.RedisConfigHelper;

/**
 * 一个Redis操作的模版类, 自动从redis pool中获取链接并在处理完后释放掉链接.
 * 封装了常用的一些redis操作, 对于复杂的带有事务或者带有失效时间的操作, 需要自己通过
 * RedisCallback实现相关操作
 * 
 * 
 */
public abstract class RedisTemplate {
	
	public abstract String getSourceName();
	
	/**
	 *	封装了redis池获取链接的操作 
	 * 
	 */
	public <T> T execute(RedisCallback<T> action)throws RedisException{
		Jedis jedis = null;
		boolean errHappend = false;
		try{
			jedis = RedisConfigHelper.getJedis(getSourceName());
			if(jedis != null){
				return action.doInRedisClient(jedis);
			}else{
				throw new RedisException("could not find redis connection from jedis pool");
			}
		}catch (Exception e) {
			errHappend = true;
			throw new RedisException("execute redis error:",e);
		}finally{
			if(jedis != null){
				RedisConfigHelper.returnJedis(getSourceName(), jedis, errHappend);
			}
		}
	} 
	
	/**
	 *	批量返回哈希表 key 中多个给定域 fields 的值
	 *  多个给定域的值。当给定域不存在或是给定 key不存在时，返回null 
	 */
	public List<String> hmget(final String key, final String[] fields)throws RedisException{
		return execute( new RedisCallback<List<String>>() {
			public List<String> doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.hmget(key, fields);
			}
		});
	}
	
	public Long zcard(final String key)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.zcard(key);
			}
		});
	}
	
	public Long expire(final String key, final int seconds) throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException {
				return jedis.expire(key, seconds);
			}
		});
	}
	
	public Long ttl(final String key) throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException {
				return jedis.ttl(key);
			}
		});
	}
	
	/**
	 * 删除给定的一个或者多个keys, 不存在的key会被忽略 
	 * 返回被删除的key的数量
	 */
	public Long del(final String...keys) throws RedisException{
		return execute(new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
 				return jedis.del(keys);
 			}
		});
	}
	
	/**
	 *	检查给定的key是否存在 
	 * 	若key存在则返回 true, 否则返回false;
	 */
	public boolean exists(final String key)throws RedisException{
		return execute(new RedisCallback<Boolean>() {
			public Boolean doInRedisClient(Jedis jedis) throws RedisException{
 				return jedis.exists(key);
 			}
		});
	}
	
	/**
	 *	将字符串value关联到key, 如果key已经有其他值,set就会覆写旧值,无视类型 
	 * 	对于某个原本带有生存时间（TTL）的键来说， 当 SET 命令成功在这个键上执行时，这个键原有的 TTL 将被清除
	 */
	public String set(final String key, final String value) throws RedisException{
		return execute( new RedisCallback<String>() {
			public String doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.set(key, value);
			}
		});
	}
	
	/**
	 *	返回 key 所关联的字符串值
	 * 	如果 key不存在那么返回特殊值null,
	 *  假如 key 储存的值不是字符串类型，返回一个错误
	 */
	public String get(final String key) throws RedisException{
		return execute(new RedisCallback<String>() {
			public String doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.get(key);
			}
		});
	}
	
	/**
	 *	将key所存储的值加上增1
	 * 	如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令
	 * 	如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误
	 *  
	 *  返回 执行完incr的值
	 */
	public Long incr(final String key) throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.incr(key);
			}
		});
	}
	
	/**
	 *	将key所存储的值加上增量increment
	 * 	如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令
	 * 	如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误
	 * 
	 * 	返回 执行完incrBy的值
	 */
	public Long incrBy(final String key, final Long increment) throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.incrBy(key, increment);
			}
		});
	}
	
	
	/**
	 * 将 key 中储存的数字值减一。
	 * 如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 DECR 操作。
	 * 如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误
	 * 
	 * 返回 执行完decr的值
	 */
	public Long decr(final String key) throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.decr(key);
			}
		});
	}
	
	/**
	 *	将key所存储的值减去减量 decrement 
	 * 	如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCRBY 命令
	 * 	如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误
	 * 
	 * 	返回 执行完decrBy的值
	 */
	public Long decrBy(final String key, final Long decrement ) throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.decrBy(key, decrement );
			}
		});
	}
	
	/**
	 * 将哈希表 key 中的域 field 的值设为 value 
	 * 如果 key 不存在，一个新的哈希表被创建并进行 HSET 操作
	 * 如果域 field 已经存在于哈希表中，旧值将被覆盖
	 * 返回: 	
	 * 	如果 field 是哈希表中的一个新建域，并且值设置成功，返回 1 
	 *  如果哈希表中域 field 已经存在且旧值已被新值覆盖，返回 0
	 */
	public Long hset(final String key, final String field,final String value)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.hset(key, field, value);
			}
		});
	}
	
	/**
	 *	返回哈希表 key 中给定域 field 的值
	 *  给定域的值。当给定域不存在或是给定 key不存在时，返回null 
	 */
	public String hget(final String key, final String field)throws RedisException{
		return execute( new RedisCallback<String>() {
			public String doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.hget(key, field);
			}
		});
	}
	
	/**
	 * 查看哈希表 key 中，给定域 field 是否存在
	 * 如果哈希表含有给定域，返回true
	 * 如果哈希表不含有给定域，或 key 不存在，返回false
	 */
	public boolean hexists(final String key, final String field)throws RedisException{
		return execute( new RedisCallback<Boolean>() {
			public Boolean doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.hexists(key, field);
			}
		});
	}
	
	/**
	 * 删除哈希表 key 中的一个或多个指定域，不存在的域将被忽略
	 * 返回被成功移除的域的数量，不包括被忽略的域
	 */
	public Long hdel(final String key, final String... fields)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.hdel(key, fields);
			}
		});
	}
	
	/**
	 *	返回哈希表 key 中，所有的域和值。 
	 * 	以列表形式返回哈希表的域和域的值。若 key 不存在，返回空列表
	 */
	public Map<String, String> hgetAll(final String key)throws RedisException{
		return execute( new RedisCallback<Map<String, String>>() {
			public Map<String, String> doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.hgetAll(key);
			}
		});
	}
	
	/**
	 * 返回哈希表 key中的所有域
	 * 一个包含哈希表中所有域的表。当 key 不存在时，返回一个空表
	 * 
	 */
	public Set<String> hkeys(final String key)throws RedisException{
        return execute( new RedisCallback<Set<String>>() {
            public Set<String> doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.hkeys(key);
            }
        });
	}
	
	/**
	 * 为哈希表 key 中的域 field 的值加上增量 increment 
	 * 增量也可以为负数，相当于对给定域进行减法操作
	 * 如果 key 不存在，一个新的哈希表被创建并执行 HINCRBY 命令
	 */
   public Long hincrBy(final String key, final String field, final long value)throws RedisException{
        return execute( new RedisCallback<Long>() {
            public Long doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.hincrBy(key, field, value);
            }
        });
    }
	
	/**
	 * 表尾插入
	 * 将一个或多个值 value 插入到列表 key的表尾(最右边)。 
	 * 如果 key 不存在，一个空列表会被创建并执行 RPUSH 操作
	 * 当 key 存在但不是列表类型时，返回一个错误
	 * 主意: rpush list a b c --> [a,b,c]
	 * 返回: 执行 RPUSH 操作后，表的长度
	 */
	public Long rpush(final String key, final String... strings)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.rpush(key, strings);
			}
		});
	}
	
	/**
	 * 表头插入
	 * 将一个或多个值 value 插入到列表 key的表头 
	 * 如果 key 不存在，一个空列表会被创建并执行 LPUSH操作
	 * 当 key 存在但不是列表类型时，返回一个错误
	 * 主意:lpush list a b c --> [c,b,a] 
	 * 返回: 执行 RPUSH 操作后，表的长度
	 */
	public Long lpush(final String key, final String... strings)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.lpush(key, strings);
			}
		});
	}
	
	/**
	 * 	返回列表key的长度
	 * 	如果key不存在，则key被解释为一个空列表，返回 0;
	 *
	 *	返回: 列表 key 的长度
	 */
	public Long llen(final String key)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.llen(key);
			}
		});
	}
	
	/**
	 * 移除并返回列表 key 的头元素
	 * 列表的头元素。
	 * 当 key 不存在时，返回 null
	 */
	public String lpop(final String key)throws RedisException{
		return execute( new RedisCallback<String>() {
			public String doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.lpop(key);
			}
		});
	}
	
	
	
	
	/**
	 * 移除并返回列表 key 的尾元素
	 * 列表的尾元素。
	 * 当 key 不存在时，返回 null
	 */
	public String rpop(final String key)throws RedisException{
		return execute( new RedisCallback<String>() {
			public String doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.rpop(key);
			}
		});
	}
	
	
	/**
	 *	blocking lpop 
	 *  阻塞，直到等待超时或发现可弹出元素为止
	 */
	public List<String>  blpop(final String key)throws RedisException{
		return execute( new RedisCallback<List<String>>() {
			public List<String>  doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.blpop(0,key);
			}
		});
	}
	
	/**
	 *	blocking rpop
	 *  阻塞，直到等待超时或发现可弹出元素为止
	 */
	public List<String> brpop(final String key)throws RedisException{
		return execute( new RedisCallback<List<String>>() {
			public List<String>  doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.brpop(key);
			}
		});
	}
	
	/**
	 * 将一个或多个 member 元素加入到集合 key 当中，已经存在于集合的 member 元素将被忽略
	 * 假如 key 不存在，则创建一个只包含 member 元素作成员的集合。
	 * 当 key 不是集合类型时，返回一个错误
	 *
	 * 返回: 被添加到集合中的新元素的数量，不包括被忽略的元素 
	 */
	public Long sadd(final String key, final String... members)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.sadd( key,members);
			}
		});
	}
	
	
	/**
	 * 返回集合 key 的基数(集合中元素的数量),当 key 不存在时，返回 0
	 * 
	 */
	public Long scard(final String key)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.scard( key);
			}
		});
	}
	
	/**
	 *	判断 member 元素是否集合 key 的成员 
	 * 
	 * 如果 member 元素是集合的成员，返回TRUE; 
	 * 如果 member 元素不是集合的成员，或 key 不存在，返回FALSE;
	 */
	public Boolean sismember(final String key, final String member)throws RedisException{
		return execute( new RedisCallback<Boolean>() {
			public Boolean doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.sismember(key, member);
			}
		});
	}
	
	
	/**
     *  返回集合 key 中的所有成员。
     *  不存在的 key 被视为空集合。
     * 
     * 如果 member 元素是集合的成员，返回TRUE; 
     * 如果 member 元素不是集合的成员，或 key 不存在，返回FALSE;
     */
    public Set<String> smembers(final String key)throws RedisException{
        return execute( new RedisCallback<Set<String>>() {
            public Set<String> doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.smembers(key);
            }
        });
    }
	
	public Long zadd(final String key, final double score, final String member)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.zadd( key, score, member);
			}
		});
	}
	
	public Long zadd(final String key, final Map<String, Double> scoreMembers)throws RedisException{
		return execute( new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.zadd(key, scoreMembers);
			}
		});
	}
	
	
	public Set<String> zrange(final String key, final long start, final long end)throws RedisException{
		return execute( new RedisCallback<Set<String>>() {
			public Set<String> doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.zrange(key, start, end);
			}
		});
	}
	
	public Set<String> zrevrange(final String key, final long start, final long end)throws RedisException{
		return execute( new RedisCallback<Set<String>>() {
			public Set<String> doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.zrevrange(key, start, end);
			}
		});
	}
	
	
	public Double zincrby(final String key, final double score,
		    final String member)throws RedisException{
		return execute( new RedisCallback<Double>() {
			public Double doInRedisClient(Jedis jedis) throws RedisException{
				return jedis.zincrby(key, score, member);
			}
		});
	}
	
	/**
	 * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。
	 * 有序集成员按 score 值递减(从大到小)的次序排列 
	 * O(log(N)+M)
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(final String key,final double max,final double min)throws RedisException{
	    return execute( new RedisCallback< Set<Tuple>>() {
            public  Set<Tuple> doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.zrevrangeByScoreWithScores(key, max, min);
            }
        });
	} 
	
	/**
	 * 返回有序集 key 中， score 值介于 max 和 min 之间(默认包括等于 max 或 min )的所有的成员。
	 * @param key
	 * @param max
	 * @param min
	 * @param offset
	 * @param count
	 * @return
	 * @throws RedisException
	 */
	public Set<Tuple> zrevrangeByScoreWithScores(final String key,final double max,final double min,final int offset,final int count)throws RedisException{
	    return execute( new RedisCallback< Set<Tuple>>() {
            public  Set<Tuple> doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.zrevrangeByScoreWithScores(key, max, min, offset, count);
            }
        });
	}
	
	/**
	 * 返回有序集 key 中，成员 member 的 score 值。
     * 如果 member 元素不是有序集 key 的成员，或 key 不存在，返回 nil
	 */
	public Double zscore(final String key, final String member) throws RedisException{
	    return execute( new RedisCallback<Double>() {
            public Double doInRedisClient(Jedis jedis) throws RedisException{
                return jedis.zscore(key, member);
            }
        });
	} 
	
	public Long publish(final String channel, final String message) throws RedisException {
		return execute(new RedisCallback<Long>() {
			public Long doInRedisClient(Jedis jedis) throws RedisException {
				return jedis.publish(channel, message);
			}
		});
	}
	
    /**
     * 获取次日凌晨时间
     * 
     * */
    public  Date getNextDayZeroTime(){
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH, 1);
        c.set(Calendar.HOUR_OF_DAY, 0); 
        c.set(Calendar.SECOND, 0); 
        c.set(Calendar.MINUTE, 0); 
        c.set(Calendar.MILLISECOND, 0); 
        return c.getTime();
    }
}
