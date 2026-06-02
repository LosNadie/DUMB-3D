const now = new Date()
const ago = (d: number, h = 0) => new Date(now.getTime() - (d * 86400 + h * 3600) * 1000).toISOString()

export const musicReviews = [
  { id: 1, albumId: 1, score: 9.2, title: 'Olivia Rodrigo - SOUR', albumTitle: 'SOUR', artist: 'Olivia Rodrigo', genre: 'POP', releaseDate: '2021-05-21', coverImage: '/mock-covers/sour.jpg', authorName: 'DUMB', publishTime: ago(0, 2), updateTime: ago(0, 2), content: '风格：POP\n年份：2021\n\n<p>Olivia Rodrigo 的首张专辑《SOUR》是一张关于青少年心碎的宣言作品。从《drivers license》到《good 4 u》，这张专辑将情感脆弱性与流行朋克的愤怒能量融合。</p>' },
  { id: 2, albumId: 2, score: 9.0, title: 'Blood Orange - Cupid Deluxe', albumTitle: 'Cupid Deluxe', artist: 'Blood Orange', genre: 'RNB', releaseDate: '2013-11-12', coverImage: '/mock-covers/cupid-deluxe.jpg', authorName: 'DUMB', publishTime: ago(1, 5), updateTime: ago(1, 5), content: "风格：R&B\n年份：2013\n\n<p>Dev Hynes 化名 Blood Orange 的《Cupid Deluxe》融合了 R&B、funk、new wave 和世界音乐。像一场穿梭于 80 年代纽约的梦。</p>" },
  { id: 3, albumId: 3, score: 8.8, title: 'Radiohead - In Rainbows', albumTitle: 'In Rainbows', artist: 'Radiohead', genre: 'ROCK', releaseDate: '2007-10-10', coverImage: '/mock-covers/in-rainbows.jpg', authorName: 'DUMB', publishTime: ago(2, 8), updateTime: ago(2, 8), content: '风格：ROCK\n年份：2007\n\n<p>《In Rainbows》是 Radiohead 最温暖、最具人性气息的专辑。《Weird Fishes/Arpeggi》营造出在深海中沉浮的迷失感。</p>' },
  { id: 4, albumId: 4, score: 9.3, title: 'Caroline Polachek - Desire, I Want to Turn Into You', albumTitle: 'Desire, I Want to Turn Into You', artist: 'Caroline Polachek', genre: 'POP', releaseDate: '2023-02-14', coverImage: '/mock-covers/desire.jpg', authorName: 'DUMB', publishTime: ago(3, 3), updateTime: ago(3, 3), content: '风格：POP\n年份：2023\n\n<p>Caroline Polachek 的《Desire》是前卫流行的大胆宣言。从《Welcome to My Island》到《Billions》，她的声乐表现力令人叹为观止。</p>' },
  { id: 5, albumId: 5, score: 8.5, title: 'toe - For Long Tomorrow', albumTitle: 'For Long Tomorrow', artist: 'toe', genre: 'ROCK', releaseDate: '2009-12-10', coverImage: '/mock-covers/for-long-tomorrow.jpg', authorName: 'DUMB', publishTime: ago(4, 12), updateTime: ago(4, 12), content: '风格：ROCK\n年份：2009\n\n<p>toe 的音乐不需要语言就能击中你。这支日本数学摇滚四重奏用复杂的拍子变化构建了一个既精密又温暖的世界。</p>' },
  { id: 6, albumId: 6, score: 9.1, title: 'Wolf Alice - Blue Weekend', albumTitle: 'Blue Weekend', artist: 'Wolf Alice', genre: 'ROCK', releaseDate: '2021-06-04', coverImage: '/mock-covers/blue-weekend.jpg', authorName: 'DUMB', publishTime: ago(5, 6), updateTime: ago(5, 6), content: '风格：ROCK\n年份：2021\n\n<p>《Blue Weekend》找到了 shoegaze 轰鸣和 folk 柔情之间的完美平衡。《The Last Man on Earth》是核心曲目。</p>' },
  { id: 7, albumId: 7, score: 8.7, title: 'Oklou - choke enough', albumTitle: 'choke enough', artist: 'Oklou', genre: 'ELECTRONIC', releaseDate: '2025-02-07', coverImage: '/mock-covers/choke-enough.jpg', authorName: 'DUMB', publishTime: ago(6, 1), updateTime: ago(6, 1), content: '风格：ELECTRONIC\n年份：2025\n\n<p>Oklou 的首张全长专辑是一个精巧的微型世界。融合了 hyperpop 的碎片化美学和 chamber pop 的温润质感。</p>' },
  { id: 8, albumId: 8, score: 8.9, title: '张悬 - 神的游戏', albumTitle: '神的游戏', artist: '张悬', genre: 'FOLK', releaseDate: '2012-08-10', coverImage: '/mock-covers/zhangxuan.jpg', authorName: 'DUMB', publishTime: ago(7, 0), updateTime: ago(7, 0), content: '风格：FOLK\n年份：2012\n\n<p>张悬的《神的游戏》是华语独立音乐的新高度——既私人又普世。《玫瑰色的你》《喜欢》皆是经典。</p>' },
]

export const animes = [
  { id: 1, title: '葬送的芙莉莲', content: '<p>勇者小队击败魔王后的故事。精灵魔法师芙莉莲在同伴离世后才开始真正理解人类的情感与生命的短暂。</p>', coverImage: '/mock-covers/frieren.jpg', backgroundImage: '/mock-covers/frieren.jpg', score: 9.0, studio: 'MADHOUSE', genre: '奇幻, 冒险, 治愈', releaseDate: '2023-09-29', publishTime: ago(4, 3), views: 10000 },
  { id: 2, title: '进击的巨人 The Final Season', content: '<p>艾伦的行动迫使每个角色都必须在忠诚和信念之间做出选择。这是整个系列的终局。</p>', coverImage: '/mock-covers/aot.jpg', backgroundImage: '/mock-covers/aot.jpg', score: 9.1, studio: 'MAPPA', genre: '黑暗, 战斗, 奇幻, 战争', releaseDate: '2020-12-07', publishTime: ago(2, 5), views: 20000 },
  { id: 3, title: '孤独摇滚！', content: '<p>极度社恐的吉他手后藤独在加入乐队后逐渐走出自我封闭的故事，精准捕捉了社恐人群的心理。</p>', coverImage: '/mock-covers/bocchi.jpg', backgroundImage: '/mock-covers/bocchi.jpg', score: 8.8, studio: 'CloverWorks', genre: '音乐, 日常, 搞笑, 青春', releaseDate: '2022-10-09', publishTime: ago(1, 8), views: 15000 },
  { id: 4, title: '咒术回战 第二季', content: '<p>以怀玉玉折和涩谷事变展开，将咒术师世界的残酷性毫无保留地呈现给观众。</p>', coverImage: '/mock-covers/jjk.jpg', backgroundImage: '/mock-covers/jjk.jpg', score: 8.4, studio: 'MAPPA', genre: '热血, 战斗, 奇幻, 黑暗', releaseDate: '2023-07-06', publishTime: ago(0, 10), views: 25000 },
  { id: 5, title: '铃芽之旅', content: '<p>新海诚将青春恋爱与自然灾难题材融合，探讨了幸存者的创伤和自我治愈。</p>', coverImage: '/mock-covers/suzume.jpg', backgroundImage: '/mock-covers/suzume.jpg', score: 8.1, studio: 'CoMix Wave Films', genre: '冒险, 奇幻, 恋爱, 灾难', releaseDate: '2022-11-11', publishTime: ago(5, 2), views: 12000 },
  { id: 6, title: 'EVA新剧场版：终', content: '<p>庵野秀明为长达四分之一世纪的福音战士系列画上了一个关于成长和放手的句号。</p>', coverImage: '/mock-covers/eva.jpg', backgroundImage: '/mock-covers/eva.jpg', score: 8.9, studio: 'Khara', genre: '科幻, 机甲, 心理, 原创', releaseDate: '2021-03-08', publishTime: ago(3, 15), views: 18000 },
]

export const movies = [
  { id: 1, title: '奥本海默', content: '<p>诺兰以惊人的内在张力诠释了这位原子弹之父的矛盾灵魂，通过三线叙事构建了关于权力和道德的宏大拼图。</p>', coverImage: '/mock-covers/oppenheimer.jpg', backgroundImage: '/mock-covers/oppenheimer.jpg', score: 8.8, director: '克里斯托弗·诺兰', actors: '基里安·墨菲, 艾米莉·布朗特, 马特·达蒙', genre: '剧情, 传记, 历史', region: '美洲', releaseDate: '2023-07-21', publishTime: ago(0, 6), views: 30000 },
  { id: 2, title: '可怜的东西', content: '<p>弗兰肯斯坦式的童话外壳下，是一个关于女性觉醒和身体自主的激进寓言。艾玛·斯通的表演令人叹服。</p>', coverImage: '/mock-covers/poor-things.jpg', backgroundImage: '/mock-covers/poor-things.jpg', score: 8.4, director: '欧格斯·兰斯莫斯', actors: '艾玛·斯通, 马克·鲁弗洛, 威廉·达福', genre: '奇幻, 喜剧, 科幻', region: '美洲', releaseDate: '2023-12-08', publishTime: ago(1, 4), views: 15000 },
  { id: 3, title: '寄生虫', content: '<p>奉俊昊以精密的类型电影语法撕开了社会阶层的伪装，从喜剧到惊悚的自然过渡展示了他对观众情绪的精湛掌控。</p>', coverImage: '/mock-covers/parasite.jpg', backgroundImage: '/mock-covers/parasite.jpg', score: 9.0, director: '奉俊昊', actors: '宋康昊, 李善均, 赵汝贞, 崔宇植', genre: '剧情, 惊悚, 喜剧', region: '亚洲', releaseDate: '2019-05-30', publishTime: ago(2, 0), views: 25000 },
  { id: 4, title: '花束般的恋爱', content: '<p>坂元裕二编剧的作品，以平凡的恋爱细节编织出关于同步和分歧的动人故事。</p>', coverImage: '/mock-covers/hanataba.jpg', backgroundImage: '/mock-covers/hanataba.jpg', score: 8.6, director: '土井裕泰', actors: '菅田将晖, 有村架纯', genre: '爱情, 剧情', region: '亚洲', releaseDate: '2021-01-29', publishTime: ago(3, 12), views: 10000 },
  { id: 5, title: '坠落的审判', content: '<p>以法庭剧为框架，解剖一段婚姻内部的权力关系和秘密。桑德拉·惠勒的表演令人不寒而栗。</p>', coverImage: '/mock-covers/anatomy-fall.jpg', backgroundImage: '/mock-covers/anatomy-fall.jpg', score: 8.7, director: '茹斯汀·特里耶', actors: '桑德拉·惠勒, 斯万·阿劳德', genre: '剧情, 犯罪, 悬疑', region: '欧洲', releaseDate: '2023-08-23', publishTime: ago(4, 8), views: 8000 },
  { id: 6, title: '瞬息全宇宙', content: '<p>狂野的多元宇宙设定下，是一个亚洲移民家庭的和解故事。杨紫琼展现了惊人的表演范围。</p>', coverImage: '/mock-covers/everything-everywhere.jpg', backgroundImage: '/mock-covers/everything-everywhere.jpg', score: 8.3, director: '关家永, 丹尼尔·施纳特', actors: '杨紫琼, 关继威, 许玮伦', genre: '奇幻, 动作, 喜剧, 家庭', region: '美洲', releaseDate: '2022-03-25', publishTime: ago(5, 2), views: 20000 },
]

export const mockComments = [
  { id: 1, contentType: 'REVIEW', contentId: 1, userId: 1, username: '乐迷', content: '写得真好！', score: 9, createTime: ago(0, 1) },
]

export const mockAuth = { token: 'mock-token', username: 'admin', role: 'admin' }

function createSuccess(data: unknown) {
  return { code: 0, message: 'ok', data }
}

export function mockFallback(url: string, _method: string): unknown {
  const method = _method.toLowerCase()
  if (method === 'get') {
    if (url === '/api/reviews') return createSuccess(musicReviews)
    if (url === '/api/anime') return createSuccess(animes)
    if (url === '/api/movie') return createSuccess(movies)
    if (url === '/api/admin/reviews') return createSuccess(musicReviews)
    if (url === '/api/admin/anime') return createSuccess(animes)
    if (url === '/api/admin/movie') return createSuccess(movies)
    if (url === '/api/comments') return createSuccess(mockComments)

    const reviewDetailMatch = url.match(/\/api\/reviews\/(\d+)/)
    if (reviewDetailMatch) {
      const id = Number(reviewDetailMatch[1])
      return createSuccess(musicReviews.find(r => r.id === id) || musicReviews[0])
    }

    const animeDetailMatch = url.match(/\/api\/anime\/(\d+)/)
    if (animeDetailMatch) {
      const id = Number(animeDetailMatch[1])
      return createSuccess(animes.find(a => a.id === id) || animes[0])
    }

    const movieDetailMatch = url.match(/\/api\/movie\/(\d+)/)
    if (movieDetailMatch) {
      const id = Number(movieDetailMatch[1])
      return createSuccess(movies.find(m => m.id === id) || movies[0])
    }

    const adminReviewDetailMatch = url.match(/\/api\/admin\/reviews\/(\d+)/)
    if (adminReviewDetailMatch) {
      const id = Number(adminReviewDetailMatch[1])
      return createSuccess(musicReviews.find(r => r.id === id) || musicReviews[0])
    }

    const adminAnimeDetailMatch = url.match(/\/api\/admin\/anime\/(\d+)/)
    if (adminAnimeDetailMatch) {
      const id = Number(adminAnimeDetailMatch[1])
      return createSuccess(animes.find(a => a.id === id) || animes[0])
    }

    const adminMovieDetailMatch = url.match(/\/api\/admin\/movie\/(\d+)/)
    if (adminMovieDetailMatch) {
      const id = Number(adminMovieDetailMatch[1])
      return createSuccess(movies.find(m => m.id === id) || movies[0])
    }
  }

  if (method === 'post') {
    if (url === '/api/auth/login' || url === '/api/auth/register') {
      return createSuccess(mockAuth)
    }
  }

  return null
}
