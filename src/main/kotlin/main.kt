import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

val articleRepository = ArticleRepository()
val memberRepository = MemberRepository()
val boardRepository = BoardRepository()

var loginedMember : Member? = null

fun main(){
    println("==게시물 프로그램 시작==")

    val articleController = ArticleController()
    val memberController = MemberController()
    val boardController = BoardController()

    articleRepository.makeTestArticles()
    memberRepository.makeTestMembers()
    boardRepository.makeTestBoard()

    while(true){

        loginedMember = memberRepository.getMemberById(1)

        val prompt = if(loginedMember == null){
            "명령어 입력 : "
        }else{
            "${loginedMember!!.nickName})"
        }
        print(prompt)
        val cmd = readLineTrim()
        val rq = Rq(cmd)
        when(rq.actionPath){

            "/exit" -> {
                println("종료")
                break
            }
            "/member/join" -> {
                memberController.join()
            }
            "/member/login" -> {
                memberController.login()
            }
            "/member/logout" -> {
                memberController.logout()
            }
            "/article/write" -> {
                articleController.add()
            }
            "/article/list" -> {
                articleController.list(rq)
            }
            "/article/detail" -> {
                articleController.detail(rq)
            }
            "/article/delete" -> {
                articleController.delete(rq)
            }
            "/article/modify" -> {
                articleController.modify(rq)
            }
            "/board/make" -> {
                boardController.make()
            }
            "/board/list" -> {
                boardController.list()
            }

        }
    }
    println("==게시물 프로그램 끝==")
}


// Board 시작
// Board DTO
data class Board(
    val id : Int,
    val name : String,
    val code : String,
    val regDate : String,
    val updateDate : String
)

// BoardRepository 시작
class BoardRepository{

    val boards = mutableListOf<Board>()
    var lastBoardId = 0

    fun addBoard(name: String, code: String) {
        val id = ++lastBoardId
        val regDate = Util.getDateNowStr()
        val updateDate = Util.getDateNowStr()
        boards.add(Board(id, name, code, regDate, updateDate))
    }

    fun makeTestBoard(){
        addBoard("공지", "notice")
        addBoard("자유", "free")
    }

    fun getBoardById(boardId: Int): Board? {
        for(board in boards){
            if(board.id == boardId){
                return board
            }
        }
        return null
    }

}

// BoardRepository 끝


// BoardController 시작
class BoardController{
    fun make() {
        print("새 게시판 이름 : ")
        val name = readLineTrim()
        print("새 게시판 코드 : ")
        val code = readLineTrim()
        boardRepository.addBoard(name, code)
        println("$name 게시판 추가완료")
    }

    fun list() {
        for(board in boardRepository.boards){
            println("번호 : ${board.id} / 이름 : ${board.name} / 코드 : ${board.code}")
        }
    }

}

// BoardController 끝

// Board 끝


// Member 시작
// Member DTO
data class Member(
    val id : Int,
    val loginId : String,
    val loginPw : String,
    val name : String,
    val nickName : String
)

// MemberRepository 시작
class MemberRepository{

    var lastMemberId = 0
    val members = mutableListOf<Member>()

    fun joinMember(loginId: String, loginPw: String, name: String, nickName: String): Int {
        val id = ++lastMemberId
        members.add(Member(id, loginId, loginPw, name, nickName))
        return id
    }

    fun makeTestMembers(){
        for(i in 1..20){
            joinMember("user$i", "user$i","길동이$i", "사용자$i")
        }
    }

    fun getMemberByLoginId(loginId: String): Member? {
        for(member in members){
            if(member.loginId == loginId){
                return member
            }
        }
        return null
    }

    fun getMemberById(memberId: Int): Member? {
        for(member in members){
            if(member.id == memberId){
                return member
            }
        }
        return null
    }

}

// MemberRepository 끝


// MemberController 시작
class MemberController{
    fun join() {
        print("사용할 아이디 입력 : ")
        val loginId = readLineTrim()
        val member = memberRepository.getMemberByLoginId(loginId)
        if(member != null){
            println("사용중인 아이디입니다.")
            return
        }
        print("사용할 비밀번호 입력 : ")
        val loginPw = readLineTrim()
        print("이름 입력 : ")
        val name = readLineTrim()
        print("별명 입력 : ")
        val nickName = readLineTrim()
        val id = memberRepository.joinMember(loginId, loginPw, name, nickName)
        println("$id 번 회원으로 가입 완료")
    }

    fun login() {
        print("아이디 입력 : ")
        val loginId = readLineTrim()
        val member = memberRepository.getMemberByLoginId(loginId)
        if(member == null){
            println("존재하지 않는 아이디입니다.")
            return
        }
        print("비밀번호 입력 : ")
        val loginPw = readLineTrim()
        if(member.loginPw != loginPw){
            println("비밀번호가 틀립니다.")
            return
        }
        loginedMember = member
        println("${member.nickName}님 환영합니다.")
    }

    fun logout() {
        loginedMember = null
        println("로그아웃")
    }

}

// MemberController 끝

// Member 끝


// Article 시작
// Article DTO
data class Article(
    val id : Int,
    var title : String,
    var body : String,
    val memberId : Int,
    val boardId : Int,
    val regDate : String,
    var updateDate : String
)

// ArticleRepository 시작
class ArticleRepository{

    val articles = mutableListOf<Article>()
    var lastArticleId = 0

    fun addArticle(title: String, body: String, memberId : Int, boardId : Int): Int {
        val id = ++lastArticleId
        val regDate = Util.getDateNowStr()
        val updateDate = Util.getDateNowStr()
        articles.add(Article(id, title, body, memberId, boardId, regDate, updateDate))
        return id
    }

    fun makeTestArticles(){
        for(i in 1..20){
            addArticle("제목$i", "내용$i", i % 9 + 1, i % 2 + 1)
        }
    }

    fun getArticleById(id: Int): Article? {
        for(article in articles){
            if(article.id == id){
                return article
            }
        }
        return null
    }

    fun articlesFilter(keyword: String, boardCode: String, page : Int, pageCount : Int): List<Article> {
        val filtered1Articles = articlesFilterByKey(keyword)
        var filtered2Articles = mutableListOf<Article>()

        val startIndex = filtered1Articles.lastIndex - ((page - 1) * pageCount)
        var endIndex = startIndex - pageCount + 1
        if(endIndex < 0){
            endIndex = 0
        }
        for(i in startIndex downTo endIndex){
            filtered2Articles.add(filtered1Articles[i])
        }

        return filtered2Articles
    }

    private fun articlesFilterByKey(keyword: String): List<Article> {
        val filteredArticles = mutableListOf<Article>()
        for(article in articles){
            if(article.title.contains(keyword)){
                filteredArticles.add(article)
            }
        }
        return filteredArticles
    }


}
// ArticleRepository 끝


// ArticleController 시작
class ArticleController{
    fun add() {
        if(loginedMember == null){
            println("로그인 후 이용해주세요.")
            return
        }
        var boardSelectStr = ""
        for(board in boardRepository.boards){
            if(boardSelectStr.length > 0){
                boardSelectStr += ", "
            }
            boardSelectStr += "${board.id} = ${board.name}"
        }
        println("(${boardSelectStr})")
        print("게시판 선택(번호) : ")
        val boardId = readLineTrim().toInt()
        print("제목 입력 : ")
        val title = readLineTrim()
        print("내용 입력 : ")
        val body = readLineTrim()
        val memberId = loginedMember!!.id
        val id = articleRepository.addArticle(title, body, memberId, boardId)
        println("$id 번 게시물 등록완료")
    }

    fun list(rq : Rq) {
        val keyword = rq.getStringParam("keyword","")
        val boardCode = rq.getStringParam("boardCode","")
        val page = rq.getIntParam("page",1)
        val pageCount = 5

        val filteredArticles = articleRepository.articlesFilter(keyword, boardCode, page, pageCount)
        for(article in filteredArticles){
            val member = memberRepository.getMemberById(article.memberId)
            val nickName = member!!.nickName

            val board = boardRepository.getBoardById(article.boardId)
            val boardName = board!!.name

            println("번호 : ${article.id} / 게시판 : ${boardName} / 제목 : ${article.title} / 작성자 : ${nickName} / 등록일 : ${article.regDate}")
        }

    }

    fun detail(rq: Rq) {
        val id = rq.getIntParam("id",0)
        if(id == 0){
            println("게시물 번호를 입력해주세요.")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        println("번호 : ${article.id}")
        println("제목 : ${article.title}")
        println("내용 : ${article.body}")
        println("등록일 : ${article.regDate}")
        println("수정일 : ${article.updateDate}")
    }

    fun delete(rq: Rq) {
        if(loginedMember == null){
            println("로그인 후 이용해주세요.")
            return
        }
        val id = rq.getIntParam("id",0)
        if(id == 0){
            println("게시물 번호를 입력해주세요.")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        if(loginedMember!!.id != article.memberId){
            println("권한이 없습니다.")
            return
        }
        articleRepository.articles.remove(article)
        println("$id 번 게시물 삭제완료")
    }

    fun modify(rq: Rq) {
        if(loginedMember == null){
            println("로그인 후 이용해주세요.")
            return
        }
        val id = rq.getIntParam("id",0)
        if(id == 0){
            println("게시물 번호를 입력해주세요.")
            return
        }
        val article = articleRepository.getArticleById(id)
        if(article == null){
            println("없는 게시물 번호입니다.")
            return
        }
        if(loginedMember!!.id != article.memberId){
            println("권한이 없습니다.")
            return
        }
        print("새 제목 : ")
        val title = readLineTrim()
        print("새 내용 : ")
        val body = readLineTrim()
        val updateDate = Util.getDateNowStr()
        article.title = title
        article.body = body
        article.updateDate = updateDate
        println("$id 번 게시물 수정완료")
    }


}

// ArticleController 끝


// Article 끝




// 유틸 관련
fun readLineTrim() = readLine()!!.trim()

object Util{
    fun getDateNowStr() : String{
        var now = LocalDateTime.now()
        var getNowStr = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH시 mm분 ss초"))
        return getNowStr
    }
}