import os

cover_dir = 'public/mock-covers'
all_files = os.listdir(cover_dir)

def find_cover(*keywords):
    for f in all_files:
        for kw in keywords:
            if kw.lower() in f.lower():
                return '/mock-covers/' + f
    return None

lines = []
lines.append('// Auto-generated mock data with real album covers from iTunes')
lines.append('const now = new Date()')
lines.append('const ago = (d: number, h = 0) => new Date(now.getTime() - (d * 86400 + h * 3600) * 1000).toISOString()')
lines.append('')

# Music Reviews
lines.append('const musicReviews = [')
music = [
    (1, 9.2, 'Olivia Rodrigo - SOUR', 'SOUR', 'Olivia Rodrigo', 'POP', '2021-05-21',
     ['Olivia','SOUR'], '风格：POP 年份：2021  Olivia Rodrigo 的首张专辑《SOUR》是一张关于青少年心碎的宣言作品。从《drivers license》到《good 4 u》，这张专辑将情感脆弱性与流行朋克的愤怒能量融合。'),
    (2, 9.0, 'Blood Orange - Cupid Deluxe', 'Cupid Deluxe', 'Blood Orange', 'RNB', '2013-11-12',
     ['Blood','Cupid'], '风格：R&B 年份：2013  Dev Hynes 化名 Blood Orange 的《Cupid Deluxe》融合了 R&B、funk、new wave 和世界音乐。像一场穿梭于 80 年代纽约的梦。'),
    (3, 8.8, 'Radiohead - In Rainbows', 'In Rainbows', 'Radiohead', 'ROCK', '2007-10-10',
     ['Radiohead','Rainbows'], '风格：ROCK 年份：2007  《In Rainbows》是 Radiohead 最温暖、最具人性气息的专辑。《Weird Fishes/Arpeggi》营造出在深海中沉浮的迷失感。'),
    (4, 9.3, 'Caroline Polachek - Desire, I Want to Turn Into You', 'Desire, I Want to Turn Into You', 'Caroline Polachek', 'POP', '2023-02-14',
     ['Caroline','Desire'], '风格：POP 年份：2023  Caroline Polachek 的《Desire》是前卫流行的大胆宣言。从《Welcome to My Island》到《Billions》，她的声乐表现力令人叹为观止。'),
    (5, 8.5, 'toe - For Long Tomorrow', 'For Long Tomorrow', 'toe', 'ROCK', '2009-12-10',
     ['toe','Tomorrow'], '风格：ROCK 年份：2009  toe 的音乐不需要语言就能击中你。这支日本数学摇滚四重奏用复杂的拍子变化构建了一个既精密又温暖的世界。'),
    (6, 9.1, 'Wolf Alice - Blue Weekend', 'Blue Weekend', 'Wolf Alice', 'ROCK', '2021-06-04',
     ['Wolf','Weekend'], '风格：ROCK 年份：2021  《Blue Weekend》找到了 shoegaze 轰鸣和 folk 柔情之间的完美平衡。《The Last Man on Earth》是核心曲目。'),
    (7, 8.7, 'Oklou - choke enough', 'choke enough', 'Oklou', 'ELECTRONIC', '2025-02-07',
     ['Oklou','choke'], '风格：ELECTRONIC 年份：2025  Oklou 的首张全长专辑是一个精巧的微型世界。融合了 hyperpop 的碎片化美学和 chamber pop 的温润质感。'),
    (8, 8.9, '张悬 - 神的游戏', '神的游戏', '张悬', 'FOLK', '2012-08-10',
     ['张悬','游戏'], '风格：FOLK 年份：2012  张悬的《神的游戏》是华语独立音乐的新高度——既私人又普世。《玫瑰色的你》《喜欢》皆是经典。'),
]

for i, t in enumerate(music):
    mid, score, title, album, artist, genre, date, keywords, content = t
    cover = find_cover(*keywords) or '/mock-covers/default.jpg'
    pt = 'ago({0}, {1})'.format(8 - i, i * 3)
    lines.append('  {')
    lines.append('    id: {0}, albumId: {0}, score: {1},'.format(mid, score))
    lines.append("    title: '{0}', albumTitle: '{1}', artist: '{2}',".format(title, album, artist))
    lines.append("    genre: '{0}', releaseDate: '{1}',".format(genre, date))
    lines.append("    coverImage: '{0}',".format(cover))
    lines.append("    authorName: 'DUMB', publishTime: {0}, updateTime: {0},".format(pt))
    lines.append("    content: '{0}',".format(content.replace("'", "\\'")))
    lines.append('  },')
lines.append(']')
lines.append('')

# Anime
lines.append('const animes = [')
animes = [
    (1, '葬送的芙莉莲', 9.0, 'MADHOUSE', '奇幻, 冒险, 治愈', '2023-09-29', ['芙莉莲'], '勇者小队击败魔王后的故事。精灵魔法师芙莉莲在同伴离世后才开始真正理解人类的情感与生命的短暂。'),
    (2, '进击的巨人 The Final Season', 9.1, 'MAPPA', '黑暗, 战斗, 奇幻, 战争', '2020-12-07', ['巨人'], '艾伦的行动迫使每个角色都必须在忠诚和信念之间做出选择。这是整个系列的终局。'),
    (3, '孤独摇滚！', 8.8, 'CloverWorks', '音乐, 日常, 搞笑, 青春', '2022-10-09', ['孤独摇滚'], '极度社恐的吉他手后藤独在加入乐队后逐渐走出自我封闭的故事，精准捕捉了社恐人群的心理。'),
    (4, '咒术回战 第二季', 8.4, 'MAPPA', '热血, 战斗, 奇幻, 黑暗', '2023-07-06', ['咒术'], '以怀玉玉折和涩谷事变展开，将咒术师世界的残酷性毫无保留地呈现给观众。'),
    (5, '铃芽之旅', 8.1, 'CoMix Wave Films', '冒险, 奇幻, 恋爱, 灾难', '2022-11-11', ['铃芽'], '新海诚将青春恋爱与自然灾难题材融合，探讨了幸存者的创伤和自我治愈。'),
    (6, 'EVA新剧场版：终', 8.9, 'Khara', '科幻, 机甲, 心理, 原创', '2021-03-08', ['EVA'], '庵野秀明为长达四分之一世纪的福音战士系列画上了一个关于成长和放手的句号。'),
]

for i, t in enumerate(animes):
    aid, title, score, studio, genre, date, keywords, content = t
    cover = find_cover(*keywords) or '/mock-covers/default.jpg'
    pt = 'ago({0}, {1})'.format(6 - i, i * 4)
    lines.append('  {')
    lines.append("    id: {0}, title: '{1}',".format(aid, title))
    lines.append("    content: '<p>{0}</p>',".format(content.replace("'", "\\'")))
    lines.append("    coverImage: '{0}', backgroundImage: '{0}',".format(cover))
    lines.append("    score: {0}, studio: '{1}',".format(score, studio))
    lines.append("    genre: '{0}', releaseDate: '{1}',".format(genre, date))
    lines.append("    publishTime: {0}, views: {1},".format(pt, 10000 + i * 3000))
    lines.append('  },')
lines.append(']')
lines.append('')

# Movies
lines.append('const movies = [')
movies = [
    (1, '奥本海默', 8.8, '克里斯托弗·诺兰', '基里安·墨菲, 艾米莉·布朗特, 马特·达蒙', '剧情, 传记, 历史', '美洲', '2023-07-21', ['奥本海默'], '诺兰以惊人的内在张力诠释了这位原子弹之父的矛盾灵魂，通过三线叙事构建了关于权力和道德的宏大拼图。'),
    (2, '可怜的东西', 8.4, '欧格斯·兰斯莫斯', '艾玛·斯通, 马克·鲁弗洛, 威廉·达福', '奇幻, 喜剧, 科幻', '美洲', '2023-12-08', ['可怜'], '弗兰肯斯坦式的童话外壳下，是一个关于女性觉醒和身体自主的激进寓言。艾玛·斯通的表演令人叹服。'),
    (3, '寄生虫', 9.0, '奉俊昊', '宋康昊, 李善均, 赵汝贞, 崔宇植', '剧情, 惊悚, 喜剧', '亚洲', '2019-05-30', ['寄生虫'], '奉俊昊以精密的类型电影语法撕开了社会阶层的伪装，从喜剧到惊悚的自然过渡展示了他对观众情绪的精湛掌控。'),
    (4, '花束般的恋爱', 8.6, '土井裕泰', '菅田将晖, 有村架纯', '爱情, 剧情', '亚洲', '2021-01-29', ['花束'], '坂元裕二编剧的作品，以平凡的恋爱细节编织出关于同步和分歧的动人故事。'),
    (5, '坠落的审判', 8.7, '茹斯汀·特里耶', '桑德拉·惠勒, 斯万·阿劳德', '剧情, 犯罪, 悬疑', '欧洲', '2023-08-23', ['审判'], '以法庭剧为框架，解剖一段婚姻内部的权力关系和秘密。桑德拉·惠勒的表演令人不寒而栗。'),
    (6, '瞬息全宇宙', 8.3, '关家永, 丹尼尔·施纳特', '杨紫琼, 关继威, 许玮伦', '奇幻, 动作, 喜剧, 家庭', '美洲', '2022-03-25', ['瞬息'], '狂野的多元宇宙设定下，是一个亚洲移民家庭的和解故事。杨紫琼展现了惊人的表演范围。'),
]

for i, t in enumerate(movies):
    mid, title, score, director, actors, genre, region, date, keywords, content = t
    cover = find_cover(*keywords) or '/mock-covers/default.jpg'
    pt = 'ago({0}, {1})'.format(5 - i, i * 2)
    lines.append('  {')
    lines.append("    id: {0}, title: '{1}',".format(mid, title))
    lines.append("    content: '<p>{0}</p>',".format(content.replace("'", "\\'")))
    lines.append("    coverImage: '{0}', backgroundImage: '{0}',".format(cover))
    lines.append("    score: {0}, director: '{1}', actors: '{2}',".format(score, director, actors))
    lines.append("    genre: '{0}', region: '{1}', releaseDate: '{2}',".format(genre, region, date))
    lines.append("    publishTime: {0}, views: {1},".format(pt, 30000 - i * 4000))
    lines.append('  },')
lines.append(']')
lines.append('')

# API routes
lines.append('export default [')
lines.append("  { url: '/api/reviews', method: 'get', response: () => ({ code: 0, message: 'ok', data: musicReviews }) },")
lines.append("  { url: /\\/api\\/reviews\\/(\\d+)/, method: 'get', response: (_: any, path: string) => { const id = Number(path.match(/\\/api\\/reviews\\/(\\d+)/)?.[1]); return { code: 0, message: 'ok', data: musicReviews.find((r: any) => r.id === id) || musicReviews[0] }; } },")
lines.append("  { url: '/api/anime', method: 'get', response: () => ({ code: 0, message: 'ok', data: animes }) },")
lines.append("  { url: /\\/api\\/anime\\/(\\d+)/, method: 'get', response: (_: any, path: string) => { const id = Number(path.match(/\\/api\\/anime\\/(\\d+)/)?.[1]); return { code: 0, message: 'ok', data: animes.find((a: any) => a.id === id) || animes[0] }; } },")
lines.append("  { url: '/api/movie', method: 'get', response: () => ({ code: 0, message: 'ok', data: movies }) },")
lines.append("  { url: /\\/api\\/movie\\/(\\d+)/, method: 'get', response: (_: any, path: string) => { const id = Number(path.match(/\\/api\\/movie\\/(\\d+)/)?.[1]); return { code: 0, message: 'ok', data: movies.find((m: any) => m.id === id) || movies[0] }; } },")
lines.append("  { url: '/api/admin/reviews', method: 'get', response: () => ({ code: 0, message: 'ok', data: musicReviews }) },")
lines.append("  { url: '/api/admin/anime', method: 'get', response: () => ({ code: 0, message: 'ok', data: animes }) },")
lines.append("  { url: '/api/admin/movie', method: 'get', response: () => ({ code: 0, message: 'ok', data: movies }) },")
lines.append("  { url: /\\/api\\/admin\\/reviews\\/(\\d+)/, method: 'get', response: (_: any, path: string) => { const id = Number(path.match(/\\/api\\/admin\\/reviews\\/(\\d+)/)?.[1]); return { code: 0, message: 'ok', data: musicReviews.find((r: any) => r.id === id) || musicReviews[0] }; } },")
lines.append("  { url: /\\/api\\/admin\\/anime\\/(\\d+)/, method: 'get', response: (_: any, path: string) => { const id = Number(path.match(/\\/api\\/admin\\/anime\\/(\\d+)/)?.[1]); return { code: 0, message: 'ok', data: animes.find((a: any) => a.id === id) || animes[0] }; } },")
lines.append("  { url: /\\/api\\/admin\\/movie\\/(\\d+)/, method: 'get', response: (_: any, path: string) => { const id = Number(path.match(/\\/api\\/admin\\/movie\\/(\\d+)/)?.[1]); return { code: 0, message: 'ok', data: movies.find((m: any) => m.id === id) || movies[0] }; } },")
lines.append("  { url: '/api/auth/login', method: 'post', response: () => ({ code: 0, message: 'ok', data: { token: 'mock-token', username: 'admin', role: 'admin' } }) },")
lines.append("  { url: '/api/auth/register', method: 'post', response: () => ({ code: 0, message: 'ok', data: { token: 'mock-token', username: 'user', role: 'user' } }) },")
lines.append("  { url: '/api/comments', method: 'get', response: () => ({ code: 0, message: 'ok', data: [{ id: 1, contentType: 'REVIEW', contentId: 1, userId: 1, username: '乐迷', content: '写得真好，完全同意！', score: 9, createTime: ago(0, 1) }] }) },")
lines.append(']')

with open('mock/music.ts', 'w', encoding='utf-8') as f:
    f.write('\n'.join(lines))

print('Mock file written!')
print('Checking covers...')
for l in lines:
    if 'mock-covers' in l:
        print('  ' + l.strip()[:90])
