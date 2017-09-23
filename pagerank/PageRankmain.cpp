#include <iostream>
#include <fstream>
using namespace std;
const int maxpointnum = 52000;
const int pointsize = 52000; 
const int linesize = 52000;
double alpha = 0.15; //ת�Ƹ��� 
int TN = 25; //�������� 
class LinkNode {
public:
	int num; //�ڵ��� 
	LinkNode* pred; //ǰ��
	LinkNode* succ; //���
	LinkNode() {
		num = -2;
		pred = NULL;
		succ = NULL;
	} 
	LinkNode(int n): num(n), pred(NULL), succ(NULL) {}
	~LinkNode() {
		delete pred;
		delete succ;
	}
	void setLinkNode(int n) {
		num = n;
		pred = NULL;
		succ = NULL;
	}
};
class LinkList {
public:
	int* index; //��¼��Ԫ�����������е�λ��
	LinkNode** ll; //��¼�ڽӱ������
	int* num; //�ڽӱ������size 
	int curlen; //��ǰ��¼����λ��0
	double* pagerank; //�洢��Ԫ�ص�pagerankֵ 
	LinkList() {
		curlen = 0; 
		index = new int [maxpointnum+1];
		pagerank = new double [pointsize];
		ll = new LinkNode* [pointsize];
		num = new int [pointsize];
		for (int i = 0; i < pointsize; i++) {
			num[i] = 0;
		} 
		for (int i = 0; i < pointsize; i++) {
			ll[i] = new LinkNode;
		}
		for (int i = 0; i <= maxpointnum; i++) {
			index[i] = -1; //-1��ʾ��û�и���� 
		}
	} 
	void inserthead(int num) { //����һ��ͷ 
		if (num < 0 || num > maxpointnum) {
			cout<<"Error need: "<<num<<endl;
			return;
		}
		if (index[num] == -1) { //�жϵ�ȷ��Ҫ��
			ll[curlen]->num = num;
			index[num] = curlen;
			curlen++;  
		} 
	}
	void inserttail(int head, int tail) { //����һ���ڵ� 
		int indextmp = index[head];
		if (indextmp == -1) {
			inserthead(head);
			indextmp = index[head];
		}
//		cout<<"prednumtmp:"<<index[tail]<<endl;
		inserthead(tail);
		int numtmp = index[tail];
//		cout<<tail<<" indextmp: "<<indextmp<<" numtmp: "<<numtmp<<endl;		
		LinkNode* ltmp = new LinkNode(numtmp);
		if (ll[indextmp]->succ) {
			ltmp->succ = ll[indextmp]->succ;
			ll[indextmp]->succ->pred = ltmp;
		} 
		ltmp->pred = ll[indextmp];
		ll[indextmp]->succ = ltmp;
	}
	void refreshnum() { //ˢ��num���� 
		for (int i = 0; i < curlen; i++) {
			int count = 0;
			LinkNode* ltmp = ll[i]->succ;
			while (ltmp) {
				count++;
				ltmp = ltmp->succ;
			}
			num[i] = count;
		} 
	}
	void PageRank() { //ʵ��pagerank�㷨 
		//��ʼ�� 
		for (int i = 0; i < curlen; i++) {
			pagerank[i] = 1.0/curlen;
		} 
		double* pagetmp = new double [curlen];
		for (int i = 0; i < TN; i++) {
			double rcount = 0.0; //��¼�޺�̽ڵ��pagerankֵ�ܺ�
			for (int j = 0; j < curlen; j++) {
				pagetmp[j] = 0.0;
			} 
			for (int j = 0; j < curlen; j++) {
				if (num[j] > 0) { //�к�� 
					LinkNode* ltmp = ll[j]->succ;
					while (ltmp) {
						pagetmp[ltmp->num] += (1-alpha)*1.0*pagerank[j]/num[j];
						ltmp = ltmp->succ;
					} 
				} else {
					rcount += (1-alpha)*1.0*pagerank[j];
				}
			}
			for (int j = 0; j < curlen; j++) {
				pagetmp[j] += 1.0*rcount/curlen + 1.0*alpha/curlen; 
			}
			for (int j = 0; j < curlen; j++) {
				pagerank[j] = pagetmp[j];
			}
			double dcount = 0.0; //��¼�ܸ���ֵ
			for (int j = 0; j < curlen; j++) {
				dcount += pagerank[j];
			} 
			cout<<"��"<<i+1<<"�ֽ���ܸ��ʺͣ�"<<dcount<<endl;
		} 
	} 
	void display(int n) { //display indexΪn������Ԫ�� 
		cout<<ll[n]->num;
		LinkNode* ltmp = ll[n]->succ;
		while (ltmp) {
			cout<<"->"<<ll[ltmp->num]->num;
			ltmp = ltmp->succ;
		} 
		cout<<endl;
	}
};
int get_Int(char* buffer, int &sign, bool &isend) {
	while (sign[buffer] < '0' || '9' < sign[buffer]) {
		if (buffer[sign] == '\0') {
			isend = true;
			return 0;
		}
		++sign;
	} 
	isend = false;
	int re = 0;
	while ('0' <= sign[buffer] && sign[buffer] <= '9') {
		re = re * 10 + (buffer[sign++] - '0');
	}
	return re;
}
int main() {
	ifstream fin("output.txt");
	ofstream fout("pagerank.xml");
	LinkList l1;
	char* buffer = new char [100000]; //����һ���ַ������� 
	int linemax = 0;
	int linetmp = 0;
	int count = 0;
	//�����ڽӱ� 
	for (int i = 0; i < linesize; i++) {
		linetmp = 0;
		fin.getline(buffer, 100000);
		int head = -1;
		int tail = 0;
		bool isend = false; //�Ƿ����  
		while (true) {
			if (head == -1) { //ͷԪ��δ����¼ 
				//insert head
				head = get_Int(buffer, linetmp, isend);
				l1.inserthead(head); 
			} else {
				tail = get_Int(buffer, linetmp, isend);
				if (!isend) {
					//insert tail
					l1.inserttail(head, tail);
				} else {
					break;
				}
			}
			count++;
		}
	}
	l1.refreshnum();
	l1.PageRank();
	cout<<"l1.curlen:"<<l1.curlen<<endl; 
	int maxsign = 0;
	for (int i = 0; i < l1.curlen; i++) {
		if (l1.pagerank[i] > l1.pagerank[maxsign]) {
			maxsign = i;
		}
	} 
	cout<<l1.ll[maxsign]->num<<" rank:"<<l1.pagerank[l1.index[l1.ll[maxsign]->num]]<<endl;
	for (int i = 0; i < 50; i++) {
		cout<<"index:"<<i<<" num:"<<l1.ll[i]->num<<" pagerank:"<<l1.pagerank[i]<<endl;
	}
	fout<<"<?xml version=\"1.0\" encoding=\"utf-8\"?>"<<endl;
	fout<<"<docs>"<<endl;
	fout<<"	<category name=\"tsinghua\">"<<endl;
	for (int i = 0; i < l1.curlen; i++) {
		fout<<"<doc id=\""<<l1.ll[i]->num<<"\" pagerank=\""<<l1.pagerank[i]<<"\" />"<<endl;
	}
	fout<<"	</category>"<<endl;
	fout<<"</docs>"<<endl;
	cout<<"Completed"<<endl;
	fout.close();
	fin.close();
	return 0;
} 
