package kr.personal.usagifloat;
/** Stable catalog IDs; corrupt/empty saved data safely uses the whole catalog. */
final class RandomPool {
 static int[] resolve(boolean all,String saved,int count){
  boolean[] included=new boolean[count];int size=0;
  if(!all&&saved!=null)for(String token:saved.split(",")){try{int id=Integer.parseInt(token.trim());if(id>=0&&id<count&&!included[id]){included[id]=true;size++;}}catch(NumberFormatException ignored){}}
  if(all||size==0){java.util.Arrays.fill(included,true);size=count;}
  int[] result=new int[size];int at=0;for(int i=0;i<count;i++)if(included[i])result[at++]=i;return result;
 }
 static boolean contains(int[] pool,int value){for(int id:pool)if(id==value)return true;return false;}
}
