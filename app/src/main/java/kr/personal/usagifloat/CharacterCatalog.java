package kr.personal.usagifloat;
final class CharacterCatalog {
 static final String[] NAMES={"꼬리 흔들기","말랑 우사기","응원 우사기","팔짱 우사기","파란 후드 우사기"};
 private static final int[] RESOURCES={R.raw.usagi,R.raw.usagi_soft,R.raw.usagi_cheer,R.raw.usagi_closeup,R.raw.usagi_hood};
 static int normalize(int index){return index>=0 && index<RESOURCES.length?index:0;}
 static int resource(int index){return RESOURCES[normalize(index)];}
}
