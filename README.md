# SlideCardPager

卡片滑动控件，使用自定义布局实现，带有卡片回收机制，可自定义卡片切换效果。

- 主要组成部分：
	1. CardHolder
	2. CardTransforms
	3. CardRecyclePool
	4. CardState
	5. ItemSelectedInterceptor
	6. OnCardChangeListener
	7. OnItemClickListener
	8. CardAdapter
- 扩展部分
	1. CardTransforms
	2. CardAdapter

## 构思，实现思路
第一眼看到这个需求的时候，心里没底，没有现成的框架，没有见过相似的功能，只能靠着自己的理解一点点尝试。

- 前期思路：
	1. 自定义ViewGroup实现多View排列样式
	2. 属性动画实现切换效果
	
