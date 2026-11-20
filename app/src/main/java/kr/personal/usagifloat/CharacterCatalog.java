package kr.personal.usagifloat;
final class CharacterCatalog {
 static final String[] NAMES={"꼬리 흔들기","말랑 우사기","응원 우사기","팔짱 우사기","파란 후드 우사기","갸웃 우사기","뿌릿 우사기","숲속 우사기","꼬마 우사기","두리번 우사기","팔랑 귀 우사기","춤추는 우사기"};
 private static final int[] RESOURCES={R.raw.usagi,R.raw.usagi_soft,R.raw.usagi_cheer,R.raw.usagi_closeup,R.raw.usagi_hood,R.raw.usagi_huh,R.raw.usagi_puri,R.raw.usagi_forest,R.raw.usagi_tiny,R.raw.usagi_room,R.raw.usagi_ears,R.raw.usagi_dance};
 private static final int[] THUMBNAILS={R.drawable.usagi_thumb,R.drawable.usagi_soft_thumb,R.drawable.usagi_cheer_thumb,R.drawable.usagi_closeup_thumb,R.drawable.usagi_hood_thumb,R.drawable.usagi_huh_thumb,R.drawable.usagi_puri_thumb,R.drawable.usagi_forest_thumb,R.drawable.usagi_tiny_thumb,R.drawable.usagi_room_thumb,R.drawable.usagi_ears_thumb,R.drawable.usagi_dance_thumb};
 static int normalize(int index){return index>=0 && index<RESOURCES.length?index:0;}
 static int resource(int index){return RESOURCES[normalize(index)];}
 static int thumbnail(int index){return THUMBNAILS[normalize(index)];}
}
